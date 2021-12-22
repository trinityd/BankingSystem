import java.util.*;
import java.io.*;
import java.sql.*;

class sample {

  private static String driver;
  private static String url;
  private static String username;
  private static String password;

  public static void main(String argv[]) {
    if (argv.length != 1) {
      System.out.println("Need database properties filename");
    } else {
      init(argv[0]);

      try {
        Class.forName(driver);                                                                  //load the driver
        Connection con = DriverManager.getConnection(url, username, password);                  //Create the connection
        Statement stmt = con.createStatement();                                                 //Create a statement
        String query = "SELECT FIRSTNME, LASTNAME, EDLEVEL, SALARY FROM EMPLOYEE";              //The query to run
        ResultSet rs = stmt.executeQuery(query);                                                //Executing the query and storing the results in a Result Set
        while(rs.next()) {                                                                      //Loop through result set and retrieve contents of each row
          String firstname = rs.getString(1);
          String lastname = rs.getString(2);
          int edlevel = rs.getInt(3);
          double salary = rs.getDouble(4);
          System.out.println(firstname + ",\t" + lastname + "," + edlevel + ",\t\t" + salary);        //Print out each row's values to the screen
        }
        rs.close();                                                                             //Close the result set after we are done with the result set
        stmt.close();                                                                           //Close the statement after we are done with the statement
        con.close();                                                                            //Close the connection after we are done with everything
      } catch (Exception e) {
        System.out.println("Exception in main()");
        e.printStackTrace();
      }
    }
  }//main

  static void init(String filename) {
    try {
      Properties props = new Properties();                                                      //Create a new Properties object
      FileInputStream input = new FileInputStream(filename);                                    //Create a new FileInputStream object using our filename parameter
      props.load(input);                                                                        //Load the file contents into the Properties object
      driver = props.getProperty("jdbc.driver");                                                //Load the driver
      url = props.getProperty("jdbc.url");                                                      //Load the url
      username = props.getProperty("jdbc.username");                                            //Load the username
      password = props.getProperty("jdbc.password");                                            //Load the password
    } catch (Exception e) {
      System.out.println("Exception in init()");
      e.printStackTrace();
    }
  }//init
}//sample
