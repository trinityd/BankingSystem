import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.*;
import java.util.Properties;

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
	private static Statement stmt;
	private static ResultSet rs;

	// UI Integration
	private static boolean DEBUG = false;
	protected static boolean errorStatus = false;
	protected static String returnMsg = "";
	protected static final String nonLoggedInCustomerID = "-1";
	protected static final String nonLoggedInCustomerName = "";
	protected static String loggedInCustomerID = nonLoggedInCustomerID;
	protected static String loggedInCustomerName = nonLoggedInCustomerName;

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
	 * Customer login authentication
	 * @param id customer id
	 * @param pin customer pin
	 * @return the customer's name
	 */
	public static void login(String cusID, String pin) {
		System.out.println(":: LOGIN - RUNNING");
		try {
			resetErrorStatusAndReturnMsg();
			connectToDB(false);

			// Input checking
			if(cusID.length() == 0) throw new Exception("ID cannot be empty"); 
			else if (pin.length() == 0) throw new Exception("PIN cannot be empty");
			// Ranges
			int cusIDInt, pinInt;
			try {
				cusIDInt = Integer.parseInt(cusID);
			} catch(NumberFormatException err) {
				throw new Exception("ID must be a number");
			}
			if(cusIDInt < 100) throw new Exception("ID must be >= 100");
			try {
				pinInt = Integer.parseInt(pin);
			} catch(NumberFormatException err) {
				throw new Exception("PIN must be a number");
			}
			if(pinInt < 0) throw new Exception("PIN must be >= 0");

			// Check if customer exists and pin is correct, base return off that as described above header
			stmt = con.createStatement();
			String sql = String.format("SELECT PIN, NAME FROM P1.CUSTOMER WHERE ID=%s", cusID);
			rs = stmt.executeQuery(sql);
			int count = 0;
			String name = "";
			int realPinInt = -1;
			while(rs.next()){
				count++;
				// Retrieve by column name
				realPinInt = rs.getInt("PIN");
				name = rs.getString("NAME");
			}
			if(count != 1) throw new Exception("no customer with that ID exists");
			else if(realPinInt != pinInt) throw new Exception("that PIN is incorrect");
			else returnMsg = name;

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
				if(rs != null) rs.close();
				if(stmt != null) stmt.close();
				if(con != null) con.close();
			} catch(Exception err) {
				if(DEBUG) err.printStackTrace();
			}
		}
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

			// Input checking
			int ageInt, pinInt;

			if(name.length() == 0) throw new Exception("INVALID NAME"); 
			else if (gender.length() == 0 || !(gender.equals("M") || gender.equals("F"))) throw new Exception("INVALID GENDER");
			else if (age.length() == 0) throw new Exception("INVALID AGE");
			else if (pin.length() == 0) throw new Exception("INVALID PIN");

			// Ranges
			try {
				ageInt = Integer.parseInt(age);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID AGE");
			}
			if(ageInt < 0) throw new Exception("INVALID AGE");
			try {
				pinInt = Integer.parseInt(pin);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID PIN");
			}
			if(pinInt < 0) throw new Exception("INVALID PIN");

			stmt = con.createStatement();
			String sql = String.format("SELECT ID FROM FINAL TABLE (INSERT INTO P1.CUSTOMER(name, gender, age, pin) VALUES('%s', '%s', %s, %s))", name, gender, age, pin);
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				 // Retrieve by column name
				 returnMsg = Integer.toString(rs.getInt("ID"));
			}
			con.commit();
			System.out.println(":: CREATE NEW CUSTOMER - SUCCESS");
		} catch(Exception err) {
			if(DEBUG) err.printStackTrace();
			errorStatus = true;
			returnMsg = err.getMessage();
			System.out.println(":: CREATE NEW CUSTOMER - ERROR - " + err.getMessage());
			try {
				con.rollback();
			} catch(Exception err2) {
				if(DEBUG) err2.printStackTrace();
			}
		} finally {
			try {
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
			
			// Input checking
			int amountInt;

			if(id.length() == 0) throw new Exception("INVALID ID"); 
			else if (type.length() == 0 || !(type.equals("C") || type.equals("S"))) throw new Exception("INVALID TYPE");
			else if (amount.length() == 0) throw new Exception("INVALID AMOUNT");

			// Ranges
			try {
				amountInt = Integer.parseInt(amount);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID AMOUNT");
			}
			if(amountInt < 0) throw new Exception("INVALID AMOUNT");

			// Check if customer exists
			stmt = con.createStatement();
			String sql = String.format("SELECT COUNT(*) AS COUNT FROM P1.CUSTOMER WHERE ID=%s", id);
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				// Retrieve by column name
				int count = rs.getInt("COUNT");
				if(count != 1) throw new Exception("NO SUCH CUSTOMER");
			}

			sql = String.format("SELECT NUMBER FROM FINAL TABLE (INSERT INTO P1.ACCOUNT(id, balance, type, status) VALUES(%s, %s, '%s', 'A'))", id, amount, type);
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				 // Retrieve by column name
				 returnMsg = Integer.toString(rs.getInt("NUMBER"));
			}
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

			// Input checking
			int accNumInt;
			if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 

			// Ranges
			try {
				accNumInt = Integer.parseInt(accNum);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID ACCNUM");
			}
			if(accNumInt < 1000) throw new Exception("INVALID ACCNUM");

			stmt = con.createStatement();
			String sql;
			// Check if account exists and is active and is owned by the currently logged in user (if there is one), if not error
			sql = String.format("SELECT ID FROM P1.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
			rs = stmt.executeQuery(sql);
			int count = 0;
			int id = 0;
			while(rs.next()){
				// Retrieve by column name
				count++;
				id = rs.getInt("ID");
			}
			if(count != 1) throw new Exception("NO SUCH ACTIVE ACCOUNT");
			else if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID) && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT CLOSE ACCOUNT YOU DON'T OWN");

			// Close account (set status to 'I' and balance to 0)
			sql = String.format("UPDATE P1.ACCOUNT SET STATUS='I', BALANCE=0 WHERE NUMBER=%s", accNum);
			stmt.executeUpdate(sql);
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

			// Input checking
			int accNumInt, amountInt;
			if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 
			else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 

			// Ranges
			try {
				accNumInt = Integer.parseInt(accNum);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID ACCNUM");
			}
			if(accNumInt < 1000) throw new Exception("INVALID ACCNUM");
			try {
				amountInt = Integer.parseInt(amount);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID AMOUNT");
			}
			if(amountInt < 0) throw new Exception("INVALID AMOUNT");

			stmt = con.createStatement();
			String sql;
			// Check if account exists and is active, if not error
			sql = String.format("SELECT COUNT(*) AS COUNT FROM P1.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
			rs = stmt.executeQuery(sql);
			int count;
			while(rs.next()){
				 // Retrieve by column name
				 count = rs.getInt("count");
				 if(count != 1) throw new Exception("NO SUCH ACTIVE ACCOUNT");
			}
			// Deposit into account
			sql = String.format("UPDATE P1.ACCOUNT SET BALANCE=(BALANCE + %s) WHERE NUMBER=%s", amount, accNum);
			stmt.executeUpdate(sql);
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

			// Input checking
			int accNumInt, amountInt;
			if(accNum.length() == 0) throw new Exception("INVALID ACCNUM"); 
			else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 

			// Ranges
			try {
				accNumInt = Integer.parseInt(accNum);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID ACCNUM");
			}
			if(accNumInt < 1000) throw new Exception("INVALID ACCNUM");
			try {
				amountInt = Integer.parseInt(amount);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID AMOUNT");
			}
			if(amountInt < 0) throw new Exception("INVALID AMOUNT");

			stmt = con.createStatement();
			String sql;
			// Check if account exists, is active, is owned by logged in user (if there is one) and has enough money for withdrawal, if not error
			sql = String.format("SELECT BALANCE, ID FROM P1.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
			rs = stmt.executeQuery(sql);
			int count = 0;
			int balance = -1;
			int id = 0;
			while(rs.next()){
				 // Retrieve by column name
				 count++;
				 balance = rs.getInt("BALANCE");
				 id = rs.getInt("ID");
			}
			if(count != 1) throw new Exception("NO SUCH ACTIVE ACCOUNT");
			else if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID) && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT TRANSFER FROM ACCOUNT YOU DON'T OWN");
			else if(balance < amountInt) throw new Exception("NOT ENOUGH FUNDS");

			// Withdraw from account
			sql = String.format("UPDATE P1.ACCOUNT SET BALANCE=(BALANCE - %s) WHERE NUMBER=%s", amount, accNum);
			stmt.executeUpdate(sql);
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

			// WITHDRAW
			String accNum = srcAccNum;

			// Input checking
			int accNumInt, amountInt;
			if(accNum.length() == 0) throw new Exception("INVALID SRCACCNUM"); 
			else if(amount.length() == 0) throw new Exception("INVALID AMOUNT"); 

			// Ranges
			try {
				accNumInt = Integer.parseInt(accNum);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID SRCACCNUM");
			}
			if(accNumInt < 1000) throw new Exception("INVALID SRCACCNUM");
			try {
				amountInt = Integer.parseInt(amount);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID AMOUNT");
			}
			if(amountInt < 0) throw new Exception("INVALID AMOUNT");

			stmt = con.createStatement();
			String sql;
			// Check if account exists, is active, is owned by logged in user (if there is one) and has enough money for withdrawal, if not error
			sql = String.format("SELECT BALANCE, ID FROM P1.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
			rs = stmt.executeQuery(sql);
			int count = 0;
			int balance = -1;
			int id = 0;
			while(rs.next()){
				 // Retrieve by column name
				 count++;
				 balance = rs.getInt("BALANCE");
				 id = rs.getInt("ID");
			}
			if(count != 1) throw new Exception("NO SUCH ACTIVE SOURCE ACCOUNT");
			else if(Integer.parseInt(loggedInCustomerID) != Integer.parseInt(nonLoggedInCustomerID) && Integer.parseInt(loggedInCustomerID) != id) throw new Exception("CANNOT TRANSFER FROM ACCOUNT YOU DON'T OWN");
			else if(balance < amountInt) throw new Exception("NOT ENOUGH FUNDS");

			// Withdraw from account
			sql = String.format("UPDATE P1.ACCOUNT SET BALANCE=(BALANCE - %s) WHERE NUMBER=%s", amount, accNum);
			stmt.executeUpdate(sql);
			con.commit();
	

			// DEPOSIT
			accNum = destAccNum;
			// Input checking
			if(accNum.length() == 0) throw new Exception("INVALID DESTACCNUM"); 

			// Ranges
			try {
				accNumInt = Integer.parseInt(accNum);
			} catch(NumberFormatException err) {
				throw new Exception("INVALID DESTACCNUM");
			}
			if(accNumInt < 1000) throw new Exception("INVALID DESTACCNUM");

			stmt = con.createStatement();
			// Check if account exists and is active, if not error
			sql = String.format("SELECT COUNT(*) AS COUNT FROM P1.ACCOUNT WHERE NUMBER=%s AND STATUS='A'", accNum);
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				 // Retrieve by column name
				 count = rs.getInt("count");
				 if(count != 1) throw new Exception("NO SUCH ACTIVE DEST ACCOUNT");
			}
			// Deposit into account
			sql = String.format("UPDATE P1.ACCOUNT SET BALANCE=(BALANCE + %s) WHERE NUMBER=%s", amount, accNum);
			stmt.executeUpdate(sql);
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

			// Input checking
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
			sql = String.format("SELECT COUNT(*) AS COUNT FROM P1.CUSTOMER WHERE ID=%s", cusID);
			rs = stmt.executeQuery(sql);
			int count;
			while(rs.next()){
				 // Retrieve by column name
				 count = rs.getInt("count");
				 if(count != 1) throw new Exception("NO SUCH CUSTOMER");
			}
			// Summarize Active Accounts
			sql = String.format("SELECT NUMBER, BALANCE FROM P1.ACCOUNT WHERE ID=%s AND STATUS='A'", cusID);
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
			sql = String.format("SELECT ID, NAME, GENDER, AGE, ISNULL(TOTAL,0) AS TOTAL FROM P1.CUSTOMER LEFT OUTER JOIN (SELECT ID AS ID2, SUM(BALANCE) AS TOTAL FROM P1.ACCOUNT GROUP BY ID ORDER BY TOTAL DESC) AS TABLE2 ON ID=ID2 ORDER BY TOTAL DESC");
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

			// Input checking
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
			sql = String.format("SELECT AVG(TOTAL) AS AVERAGE FROM P1.CUSTOMER INNER JOIN (SELECT ID AS ID2, SUM(BALANCE) AS TOTAL FROM P1.ACCOUNT WHERE STATUS='A' GROUP BY ID ORDER BY TOTAL DESC) AS TABLE2 ON ID=ID2 WHERE AGE >= %s AND AGE <= %s", min, max);
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
				if(rs != null) rs.close();
				if(stmt != null) stmt.close();
				if(con != null) con.close();
			} catch(Exception err) {
				if(DEBUG) err.printStackTrace();
			}
		}	
	}
}
