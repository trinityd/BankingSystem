import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.*;
import java.util.Properties;
import java.sql.CallableStatement;
import java.sql.Types;

/**
 * Manage connection to database and perform SQL statements.
 */
public class BankingSystem {
	// Connection properties
	private static String driver;
	private static String url;
	private static String username;
	private static String password;
	
	// JDBC Objects
	private static Connection con;
	private static CallableStatement cs;
  private static Statement stmt;
	private static ResultSet rs;

  // UI Integration
  private static boolean DEBUG = false;
  protected static boolean errorStatus = false;
  protected static String returnMsg = "";
  protected static final String nonLoggedInCustomerID = "-1";
  protected static String loggedInCustomerID = nonLoggedInCustomerID;

	/**
	 * Initialize database connection given properties file.
	 * @param filename name of properties file
	 */
	public static void init(String filename) {
		try {
			Properties props = new Properties();						// Create a new Properties object
			FileInputStream input = new FileInputStream(filename);	// Create a new FileInputStream object using our filename parameter
			props.load(input);										// Load the file contents into the Properties object
			driver = props.getProperty("jdbc.driver");				// Load the driver
			url = props.getProperty("jdbc.url");						// Load the url
			username = props.getProperty("jdbc.username");			// Load the username
			password = props.getProperty("jdbc.password");			// Load the password
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test database connection.
	 */
	public static void testConnection() {
		System.out.println(":: TEST - CONNECTING TO DATABASE");
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, username, password);
			con.close();
			System.out.println(":: TEST - SUCCESSFULLY CONNECTED TO DATABASE");
			} catch (Exception e) {
				System.out.println(":: TEST - FAILED CONNECTED TO DATABASE");
				e.printStackTrace();
			}
	  }

  /**
	 * Added helper:
   * Connect to database and stay connected.
   * @param autoCommit commit changes automatically (false is safest and what's used throughout)
	 */
	public static void connectToDB(boolean autoCommit) throws Exception {
		try {
      Class.forName(driver);
      con = DriverManager.getConnection(url, username, password);
      con.setAutoCommit(autoCommit); // Commit changes automatically (true)/manually (false)
    } catch (Exception e) {
			throw new Exception("FAILED TO CONNECT TO DATABASE");
    }
  }

  /**
   * Added helper:
	 * Resets error status and return message (used in UI)
	 */
  public static void resetErrorStatusAndReturnMsg() {
    errorStatus = false;
    returnMsg = "";
  }

	/**
	 * Create a new customer.
	 * @param name customer name
	 * @param gender customer gender
	 * @param age customer age
	 * @param pin customer pin
	 */
	public static void newCustomer(String name, String gender, String age, String pin) 
	{
		System.out.println(":: CREATE NEW CUSTOMER - RUNNING");
    try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int ageInt, pinInt;

      if(name.length() == 0) throw new Exception("INVALID NAME"); 
      else if (age.length() == 0) throw new Exception("INVALID AGE");
      else if (pin.length() == 0) throw new Exception("INVALID PIN");
      try {
        ageInt = Integer.parseInt(age);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID AGE");
      }
      try {
        pinInt = Integer.parseInt(pin);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID PIN");
      }

      String sql = "{CALL P2.CUST_CRT (?, ?, ?, ?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setString("p_name", name);
      cs.setString("p_gender", gender);
      cs.setInt("p_age", ageInt);
      cs.setInt("p_pin", pinInt);
      cs.registerOutParameter("id", Types.INTEGER);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int id = cs.getInt("id");
      returnMsg = Integer.toString(id);
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: NEW CUSTOMER - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      
      System.out.println(":: NEW CUSTOMER - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

  /**
	 * Customer login authentication
   * @param id customer id
   * @param pin customer pin
	 */
  public static void login(String cusID, String pin) {
    System.out.println(":: LOGIN - RUNNING");
    try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      if(cusID.length() == 0) throw new Exception("ID cannot be empty"); 
      else if (pin.length() == 0) throw new Exception("PIN cannot be empty");
      int cusIDInt, pinInt;
      try {
        cusIDInt = Integer.parseInt(cusID);
      } catch(NumberFormatException err) {
        throw new Exception("ID must be a number");
      }
      try {
        pinInt = Integer.parseInt(pin);
      } catch(NumberFormatException err) {
        throw new Exception("PIN must be a number");
      }

      String sql = "{CALL P2.CUST_LOGIN (?, ?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("p_id", cusIDInt);
      cs.setInt("p_pin", pinInt);
      cs.registerOutParameter("Valid", Types.INTEGER);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int valid = cs.getInt("Valid");
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      loggedInCustomerID = cusID;
      returnMsg = cusID;

      con.commit();
      System.out.println(":: LOGIN - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      
      System.out.println(":: LOGIN - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
  }

	/**
	 * Open a new account.
	 * @param id customer id
	 * @param type type of account
	 * @param amount initial deposit amount
	 */
	public static void openAccount(String id, String type, String amount) 
	{
		System.out.println(":: OPEN ACCOUNT - RUNNING");
    try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);
      
      // Input checking not handled by stored procedure
      int amountInt;
      if(id.length() == 0) throw new Exception("INVALID ID"); 
      else if (type.length() == 0) throw new Exception("INVALID TYPE");
      else if (amount.length() == 0) throw new Exception("INVALID AMOUNT");
      try {
        amountInt = Integer.parseInt(amount);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID AMOUNT");
      }
      int idInt;
      try {
        idInt = Integer.parseInt(id);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID ID");
      }

      String sql = "{CALL P2.ACCT_OPN (?, ?, ?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("p_id", idInt);
      cs.setInt("p_balance", amountInt);
      cs.setString("p_type", type);
      cs.registerOutParameter("Number", Types.INTEGER);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int number = cs.getInt("Number");
      returnMsg = Integer.toString(number);
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: OPEN ACCOUNT - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: OPEN ACCOUNT - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

	/**
	 * Close an account.
	 * @param accNum account number
	 */
	public static void closeAccount(String accNum) 
	{
		System.out.println(":: CLOSE ACCOUNT - RUNNING");
    try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int accNumInt;
      if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 
      try {
        accNumInt = Integer.parseInt(accNum);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID ACCNUM");
      }
      // Check if account is owned by logged in user (couldn't think of a way to do this in the stored procedure without adding an input parameter)
      String sql = String.format("SELECT ID FROM P2.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
      if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID)) {
        stmt = con.createStatement();
        rs = stmt.executeQuery(sql);
        int count = 0;
        int id = 0;
        while(rs.next()){
          // Retrieve by column name
          count = 1;
          id = rs.getInt("ID");
        }
        if(count == 1 && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT CLOSE ACCOUNT YOU DON'T OWN");
      }

      sql = "{CALL P2.ACCT_CLS (?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("p_number", accNumInt);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: CLOSE ACCOUNT - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: CLOSE ACCOUNT - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

	/**
	 * Deposit into an account.
	 * @param accNum account number
	 * @param amount deposit amount
	 */
  
  // Allow muting output from deposit/withdraw - useful for matching transfer test output
	public static void deposit(String accNum, String amount) 
	{
		System.out.println(":: DEPOSIT - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int accNumInt, amountInt;
      if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 
      else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 

      // Ranges
      try {
        accNumInt = Integer.parseInt(accNum);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID ACCNUM");
      }
      try {
        amountInt = Integer.parseInt(amount);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID AMOUNT");
      }

      String sql = "{CALL P2.ACCT_DEP (?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("p_number", accNumInt);
      cs.setInt("p_amt", amountInt);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: DEPOSIT - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: DEPOSIT - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

	/**
	 * Withdraw from an account.
	 * @param accNum account number
	 * @param amount withdraw amount
	 */
	public static void withdraw(String accNum, String amount) 
	{
		System.out.println(":: WITHDRAW - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int accNumInt, amountInt;
      if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 
      else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 
      try {
        accNumInt = Integer.parseInt(accNum);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID ACCNUM");
      }
      try {
        amountInt = Integer.parseInt(amount);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID AMOUNT");
      }
      // Check if account is owned by logged in user (couldn't think of a way to do this in the stored procedure without adding an input parameter)
      String sql = String.format("SELECT ID FROM P2.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
      if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID)) {
        stmt = con.createStatement();
        rs = stmt.executeQuery(sql);
        int count = 0;
        int id = 0;
        while(rs.next()){
          // Retrieve by column name
          count = 1;
          id = rs.getInt("ID");
        }
        if(count == 1 && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT WITHDRAW ACCOUNT YOU DON'T OWN");
      }

      sql = "{CALL P2.ACCT_WTH (?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("p_number", accNumInt);
      cs.setInt("p_amt", amountInt);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: WITHDRAW - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: WITHDRAW - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

	/**
	 * Transfer amount from source account to destination account. 
	 * @param srcAccNum source account number
	 * @param destAccNum destination account number
	 * @param amount transfer amount
	 */
	public static void transfer(String srcAccNum, String destAccNum, String amount) 
	{
		System.out.println(":: TRANSFER - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int srcAccNumInt, destAccNumInt, amountInt;
      if(srcAccNum.length() == 0) throw new Exception("INVALID SRCACCNUM"); 
      if(destAccNum.length() == 0) throw new Exception("INVALID DESTACCNUM"); 
      else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 
      try {
        srcAccNumInt = Integer.parseInt(srcAccNum);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID SRCACCNUM");
      }
      try {
        destAccNumInt = Integer.parseInt(destAccNum);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID DESTACCNUM");
      }
      try {
        amountInt = Integer.parseInt(amount);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID AMOUNT");
      }
      // Check if account is owned by logged in user (couldn't think of a way to do this in the stored procedure without adding an input parameter)
      String sql = String.format("SELECT ID FROM P2.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", srcAccNum);
      if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID)) {
        stmt = con.createStatement();
        rs = stmt.executeQuery(sql);
        int count = 0;
        int id = 0;
        while(rs.next()){
          // Retrieve by column name
          count = 1;
          id = rs.getInt("ID");
        }
        if(count == 1 && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT WITHDRAW FROM ACCOUNT YOU DON'T OWN");
      }

      sql = "{CALL P2.ACCT_TRX (?, ?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setInt("src_number", srcAccNumInt);
      cs.setInt("dest_number", destAccNumInt);
      cs.setInt("p_amt", amountInt);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);
      
      con.commit();
      System.out.println(":: TRANSFER - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: TRANSFER - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
	}

	/**
	 * Display account summary.
	 * @param cusID customer ID
	 */
	public static void accountSummary(String cusID) 
	{
		System.out.println(":: ACCOUNT SUMMARY - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int cusIDInt;
      if(cusID.length() == 0) throw new Exception("INVALID CUSID"); 

      // Ranges
      try {
        cusIDInt = Integer.parseInt(cusID);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID CUSID");
      }
      if(cusIDInt < 100) throw new Exception("INVALID CUSID");

      stmt = con.createStatement();
      String sql;
      // Check if customer ID exists, if not error
      sql = String.format("SELECT COUNT(*) AS COUNT FROM P2.CUSTOMER WHERE ID=%s", cusID);
      rs = stmt.executeQuery(sql);
      int count;
      while(rs.next()){
         // Retrieve by column name
         count = rs.getInt("count");
         if(count != 1) throw new Exception("NO SUCH CUSTOMER");
      }
      // Summarize Active Accounts
      sql = String.format("SELECT NUMBER, BALANCE FROM P2.ACCOUNT WHERE ID=%s AND STATUS='A'", cusID);
      rs = stmt.executeQuery(sql);
      returnMsg = String.format("%-11s %-11s\n----------- -----------\n", "NUMBER", "BALANCE");
      int number, balance;
      int total = 0;
      while(rs.next()){
         // Retrieve by column name
         number = rs.getInt("NUMBER");
         balance = rs.getInt("BALANCE");
         total += balance;
         returnMsg += String.format("%11s %11s\n", number, balance);
      }
      returnMsg += String.format("-----------------------\n%-11s %11s\n", "TOTAL", total);
      System.out.println(returnMsg);
      con.commit();
      System.out.println(":: ACCOUNT SUMMARY - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: ACCOUNT SUMMARY - ERROR - " + err.getMessage());
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }	
	}

	/**
	 * Display Report A - Customer Information with Total Balance in Decreasing Order.
	 */
	public static void reportA() 
	{
		System.out.println(":: REPORT A - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);
      stmt = con.createStatement();
      // Get report result set and display it 
      String sql;
      sql = String.format("SELECT ID, NAME, GENDER, AGE, ISNULL(TOTAL,0) AS TOTAL FROM P2.CUSTOMER LEFT OUTER JOIN (SELECT ID AS ID2, SUM(BALANCE) AS TOTAL FROM P2.ACCOUNT GROUP BY ID ORDER BY TOTAL DESC) AS TABLE2 ON ID=ID2 ORDER BY TOTAL DESC");
      rs = stmt.executeQuery(sql);
      returnMsg = String.format("%-11s %-15s %-6s %-11s %-11s\n----------- --------------- ------ ----------- -----------\n", "ID", "NAME", "GENDER", "AGE", "TOTAL");
      int id, age, total;
      String name, gender;
      while(rs.next()){
         // Retrieve by column name
         id = rs.getInt("ID");
         name = rs.getString("NAME");
         gender = rs.getString("GENDER");
         age = rs.getInt("AGE");
         total = rs.getInt("TOTAL");

         returnMsg += String.format("%11s %-15s %-6s %11s %11s\n", id, name, gender, age, total);
      }
      con.commit();
      System.out.println(returnMsg);
      System.out.println(":: REPORT A - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: REPORT A - ERROR - " + err.getMessage());
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }	
	}

	/**
	 * Display Report B - Customer Information with Total Balance in Decreasing Order.
	 * @param min minimum age
	 * @param max maximum age
	 */
	public static void reportB(String min, String max) 
	{
		System.out.println(":: REPORT B - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      int minInt, maxInt;
      if(min.length() == 0) throw new Exception("INVALID MIN"); 
      else if(max.length() == 0) throw new Exception("INVALID MAX"); 

      // Ranges
      try {
        minInt = Integer.parseInt(min);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID MIN");
      }
      if(minInt < 0) throw new Exception("INVALID MIN");
      try {
        maxInt = Integer.parseInt(max);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID MAX");
      }
      if(maxInt < 0) throw new Exception("INVALID MAX");

      stmt = con.createStatement();
      // Get report result set and display it 
      String sql;
      sql = String.format("SELECT AVG(TOTAL) AS AVERAGE FROM P2.CUSTOMER INNER JOIN (SELECT ID AS ID2, SUM(BALANCE) AS TOTAL FROM P2.ACCOUNT WHERE STATUS='A' GROUP BY ID ORDER BY TOTAL DESC) AS TABLE2 ON ID=ID2 WHERE AGE >= %s AND AGE <= %s", min, max);
      rs = stmt.executeQuery(sql);
      returnMsg = String.format("%-11s\n-----------\n", "AVERAGE");
      int average;
      while(rs.next()){
         // Retrieve by column name
         average = rs.getInt("AVERAGE");

         returnMsg += String.format("%11s\n", average);
      }
      con.commit();
      System.out.println(returnMsg);
      System.out.println(":: REPORT B - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: REPORT B - ERROR - " + err.getMessage());
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }	
	}

  /**
	 * Add Interest to all active accounts based on account type
	 */
	public static void addInterest(String savingsRate, String checkingRate) 
	{
    System.out.println(":: ADD INTEREST - RUNNING");
		try {
      resetErrorStatusAndReturnMsg();
      connectToDB(false);

      // Input checking not handled by stored procedure
      float savingsRateFloat, checkingRateFloat;
      if(savingsRate.length() == 0) throw new Exception("INVALID SAVINGS RATE"); 
      else if(checkingRate.length() == 0) throw new Exception("INVALID CHECKING RATE"); 
      try {
        savingsRateFloat = Float.parseFloat(savingsRate);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID SAVINGS RATE");
      }
      try {
        checkingRateFloat = Float.parseFloat(checkingRate);
      } catch(NumberFormatException err) {
        throw new Exception("INVALID checkingRate");
      }

      String sql = "{CALL P2.ADD_INTEREST (?, ?, ?, ?)}";
      cs = con.prepareCall(sql);
      cs.setFloat("savings_rate", savingsRateFloat);
      cs.setFloat("checking_rate", checkingRateFloat);
      cs.registerOutParameter("sql_code", Types.INTEGER);
      cs.registerOutParameter("err_msg", Types.CHAR);
      cs.execute();
      int sql_code = cs.getInt("sql_code");
      String err_msg = cs.getString("err_msg");
      if(sql_code != 0) throw new Exception(err_msg);

      con.commit();
      System.out.println(":: ADD INTEREST - SUCCESS");
    } catch(Exception err) {
      if(DEBUG) err.printStackTrace();
      errorStatus = true;
      returnMsg = err.getMessage();
      System.out.println(":: ADD INTEREST - ERROR - " + err.getMessage());
      try {
        con.rollback();
      } catch(Exception err2) {
        if(DEBUG) err2.printStackTrace();
      }
    } finally {
      try {
        if(cs != null) cs.close();
        if(rs != null) rs.close();
        if(stmt != null) stmt.close();
        if(con != null) con.close();
      } catch(Exception err) {
        if(DEBUG) err.printStackTrace();
      }
    }
  }
}
