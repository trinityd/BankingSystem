--
drop procedure p2.test4@
drop procedure p2.test3@
--
--
CREATE PROCEDURE p2.test3 
(IN jobName CHAR(8), IN gender CHAR(1), OUT jobCount INTEGER)
LANGUAGE SQL
  BEGIN
    DECLARE SQLSTATE CHAR(5);

    declare c1 cursor for
      SELECT count(jobName) 
        from employee 
      where job = jobName
        AND sex = gender;
    
    open c1;
    fetch c1 into jobCount;
    close c1;

END @

CREATE PROCEDURE p2.test4 
(IN jobName CHAR(18), IN gender CHAR(1), OUT jobCount INTEGER, OUT sql_code INTEGER, OUT err_msg VARCHAR(30))
LANGUAGE SQL
  BEGIN
    DECLARE v_count integer;

    IF gender != 'M' AND gender != 'F' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid gender';
    ELSE
      SET v_count = -1;

      CALL p2.test3(jobName, gender, v_count);
      set jobCount = v_count;
    END IF;
END @
 