--
-- db2 -td"@" -f p2.sql
--
CONNECT TO CS157A@
--
--
DROP PROCEDURE P2.CUST_CRT@
DROP PROCEDURE P2.CUST_LOGIN@
DROP PROCEDURE P2.ACCT_OPN@
DROP PROCEDURE P2.ACCT_CLS@
DROP PROCEDURE P2.ACCT_DEP@
DROP PROCEDURE P2.ACCT_WTH@
DROP PROCEDURE P2.ACCT_TRX@
DROP PROCEDURE P2.ADD_INTEREST@
--
--
CREATE PROCEDURE P2.CUST_CRT
(IN p_name CHAR(15), IN p_gender CHAR(1), IN p_age INTEGER, IN p_pin INTEGER, OUT id INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE encrypted_pin INTEGER;
    IF p_gender != 'M' AND p_gender != 'F' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid gender';
    ELSEIF p_age <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid age';
    ELSEIF p_pin < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid pin';
    ELSE
      SET encrypted_pin = p2.encrypt(p_pin);
      INSERT INTO P2.CUSTOMER(Name, Gender, Age, Pin) VALUES(p_name, p_gender, p_age, encrypted_pin);
      SET id = IDENTITY_VAL_LOCAL();
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.CUST_LOGIN
(IN p_id INTEGER, IN p_pin INTEGER, OUT Valid INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE encrypted_pin INTEGER;
    DECLARE decrypted_pin INTEGER;
    IF p_id < 0 THEN
      SET sql_code = -100;
      SET Valid = 0;
      SET err_msg = 'Invalid id';
    ELSEIF p_pin < 0 THEN
      SET sql_code = -100;
      SET Valid = 0;
      SET err_msg = 'Invalid pin';
    ELSE
      SET encrypted_pin = (SELECT Pin FROM P2.CUSTOMER WHERE ID=p_id);
      SET decrypted_pin = p2.decrypt(encrypted_pin);
      IF decrypted_pin = p_pin THEN
        SET sql_code = 0;
        SET Valid = 1;
      ELSE
        SET sql_code = -100;
        SET Valid = 0;
        SET err_msg = 'Incorrect id or pin';
      END IF;
    END IF;
END@

CREATE PROCEDURE P2.ACCT_OPN
(IN p_id INTEGER, IN p_balance INTEGER, IN p_type CHAR(1), OUT Number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE customerCount INTEGER;
    SET customerCount = (SELECT COUNT(*) AS COUNT FROM P2.CUSTOMER WHERE ID=p_id);
    IF p_id < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid customer id';
    ELSEIF p_balance < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid balance';
    ELSEIF p_type != 'C' AND p_type != 'S' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid type';
    ELSEIF customerCount != 1 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid customer id';
    ELSE
      INSERT INTO P2.ACCOUNT(id, balance, type, status) VALUES(p_id, p_balance, p_type, 'A');
      SET Number = IDENTITY_VAL_LOCAL();
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.ACCT_CLS
(IN p_number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE accountCount INTEGER;
    SET accountCount = (SELECT COUNT(*) AS COUNT FROM P2.ACCOUNT WHERE Number=p_number);
    IF p_number < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSEIF accountCount != 1 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSE
      UPDATE P2.ACCOUNT SET STATUS='I', BALANCE=0 WHERE NUMBER=p_number;
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.ACCT_DEP
(IN p_number INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE accountCount INTEGER;
    SET accountCount = (SELECT COUNT(*) FROM P2.ACCOUNT WHERE NUMBER=p_number AND STATUS='A'); -- Don't allow depositing into closed accts
    IF p_number < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSEIF p_amt < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid amount';
    ELSEIF accountCount != 1 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSE
      UPDATE P2.ACCOUNT SET BALANCE=(BALANCE + p_amt) WHERE NUMBER=p_number;
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.ACCT_WTH
(IN p_number INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE accountCount INTEGER;
    DECLARE currentBal INTEGER;
    SET accountCount = (SELECT COUNT(*) FROM P2.ACCOUNT WHERE NUMBER=p_number AND STATUS='A'); -- Don't allow withdrawing from closed accts
    IF p_number < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSEIF p_amt < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid amount';
    ELSEIF accountCount != 1 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSE
      SET currentBal = (SELECT BALANCE FROM P2.ACCOUNT WHERE NUMBER=p_number);
      IF currentBal - p_amt < 0 THEN
        SET sql_code = -100;
        SET err_msg = 'Not enough funds';
      ELSE
        UPDATE P2.ACCOUNT SET BALANCE=(BALANCE - p_amt) WHERE NUMBER=p_number;
        SET sql_code = 0;
      END IF;
    END IF;
END@

CREATE PROCEDURE P2.ACCT_TRX
(IN src_number INTEGER, IN dest_number INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    CALL P2.ACCT_WTH(src_number, p_amt, sql_code, err_msg);
    IF sql_code = 0 THEN
      CALL P2.ACCT_DEP(dest_number, p_amt, sql_code, err_msg);
    END IF;
END@

CREATE PROCEDURE P2.ADD_INTEREST
(IN savings_rate FLOAT(10), IN checking_rate FLOAT(10), OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF savings_rate < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid savings interest rate';
    ELSEIF checking_rate < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid checking interest rate';
    ELSE UPDATE P2.ACCOUNT
      SET BALANCE = CASE
        WHEN TYPE = 'S' THEN BALANCE + (BALANCE * savings_rate)
        WHEN TYPE = 'C' THEN BALANCE + (BALANCE * checking_rate)
      END
      WHERE STATUS = 'A';
      SET sql_code = 0;
    END IF;
END@
--
TERMINATE@
--
--
