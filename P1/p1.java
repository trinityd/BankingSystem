import java.util.Scanner;

/**
 * Read-Execute-Print-Loop (REPL) for BankingSystem
 * Must be run with db2jcc4.jar in same folder
 * args(0): db.properties file containing db login info in same folder 
 * Sample run on Windows Terminal: javac *.java; java -cp ";./db2jcc4.jar" p1 ./db.properties;
 */

 public class p1 extends BankingSystem {
  private static BankingSystem bankingSystem;

  private static String[][] menus = {
    {"Welcome to the Self Services Banking System! - Main Menu",
    "1.  New Customer",
    "2.  Customer Login",
    "3.  Exit"},
    {"Customer Main Menu",
    "1.  Open Account",
    "2.  Close Account", 
    "3.  Deposit", 
    "4.  Withdraw", 
    "5.  Transfer", 
    "6.  Account Summary", 
    "7.  Exit"},
    {"Administrator Main Menu",
    "1.  Account Summary for a Customer",
    "2.  Report A :: Customer Information with Total Balance in Decreasing Order", 
    "3.  Report B :: Find the Average Total Balance Between Age Groups",
    "4.  Exit"}
  };
  
  private static final int EXITPROGRAM = -1;
  private static final int MAINMENU = 0;
  private static final int CUSTOMERMENU = 1;
  private static final int ADMINMENU = 2;
  private static int menuState = MAINMENU;
  private static final String adminID = "0";
  private static final String adminPIN = "0";
  private static final String adminName = "Admin";

  public static void main(String[] args) {
    bankingSystem.init(args[0]);
    repl();
  }


  // Pure UI
  public static void displayMenu() {
    if(!loggedInCustomerName.equals(nonLoggedInCustomerName) && !loggedInCustomerID.equals(nonLoggedInCustomerID)) System.out.println("Currently Logged in as: " + loggedInCustomerName + " with ID: " + loggedInCustomerID);
    for(String line : menus[menuState]) System.out.println(line);
  }

  public static void invalidChoice(String choice) {
    String invalidChoice = String.format("Sorry, \"%s\" isn't a valid choice.\n", choice);
    System.out.println(invalidChoice);
  }

  public static void logout() {
    loggedInCustomerID = nonLoggedInCustomerID;
    loggedInCustomerName = nonLoggedInCustomerName;
    menuState = MAINMENU;
  }

  public static void repl() {
    Scanner scan = new Scanner(System.in);
    String inputStr = "";
    int inputInt = 0;
    while(menuState != EXITPROGRAM) {
      resetErrorStatusAndReturnMsg();
      displayMenu();
      System.out.print("-> ");
      try {
        inputStr = scan.nextLine();
        inputInt = Integer.parseInt(inputStr);
        System.out.println();
        switch(menuState) {
          case MAINMENU: { // Main Menu
            switch(inputInt) {
              case 1: { // New Customer
                String name, gender, age, pin;
                String firstPrompt = "Please enter the new account holder's Name: ";
                System.out.print(firstPrompt);
                name = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Gender (M/F): ");
                gender = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Age: ");
                age = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "PIN: ");
                pin = scan.nextLine().trim();

                bankingSystem.newCustomer(name, gender, age, pin);
                if(!errorStatus) {
                  System.out.format("Created new customer with Name: %s and ID: %s\n", name, returnMsg);
                }
                break;
              }
              case 2: { // Customer Login
                String cusID, pin;
                String firstPrompt = "Please enter your Customer ID: ";
                System.out.print(firstPrompt);
                cusID = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "PIN: ");
                pin = scan.nextLine().trim();
                if(cusID.equals(adminID) && pin.equals(adminPIN)) {
                  loggedInCustomerID = adminID;
                  loggedInCustomerName = adminName;
                  menuState = ADMINMENU;
                } else {
                  bankingSystem.login(cusID, pin);
                  if(!errorStatus) {
                    loggedInCustomerName = returnMsg;
                    loggedInCustomerID = cusID;
                    menuState = CUSTOMERMENU;
                  }
                }
                break;
              }
              case 3: { // Exit
                menuState = EXITPROGRAM;
                break;
              }
              default: {
                invalidChoice(inputStr);
                break;
              }
            }
            break;
          }
          case CUSTOMERMENU: { // Customer Main Menu
            switch(inputInt) {
              case 1: { // New Account
                String cusID, accType, balance;
                String firstPrompt = "Please enter the new account holder's Customer ID: ";
                System.out.print(firstPrompt);
                cusID = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Account Type (C for Checking, S for Savings): ");
                accType = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Starting Balance: ");
                balance = scan.nextLine().trim();

                bankingSystem.openAccount(cusID, accType, balance);
                if(!errorStatus) {
                  System.out.format("Opened new account with number: %s\n", returnMsg);
                }
                break;
              }
              case 2: { // Close Account
                String accNum;
                String firstPrompt = "Please enter the account's Number: ";
                System.out.print(firstPrompt);
                accNum = scan.nextLine().trim();
                bankingSystem.closeAccount(accNum);

                break;
              }
              case 3: { // Deposit
                String accNum, amount;
                String firstPrompt = "Please enter the account's Number: ";
                System.out.print(firstPrompt);
                accNum = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Amount: ");
                amount = scan.nextLine().trim();
                bankingSystem.deposit(accNum, amount);
                break;
              }
              case 4: { // Withdraw
                String accNum, amount;
                String firstPrompt = "Please enter the account's Number: ";
                System.out.print(firstPrompt);
                accNum = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Amount: ");
                amount = scan.nextLine().trim();
                bankingSystem.withdraw(accNum, amount);
                break;
              }
              case 5: { // Transfer
                String srcAccNum, destAccNum, amount;
                String firstPrompt = "Please enter the source account's Number: ";
                System.out.print(firstPrompt);
                srcAccNum = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "destination account's Number: ");
                destAccNum = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Amount: ");
                amount = scan.nextLine().trim();
                bankingSystem.transfer(srcAccNum, destAccNum, amount);
                break;
              }
              case 6: { // Logged in Account Summary
                bankingSystem.accountSummary(loggedInCustomerID);
                break;
              }
              case 7: { // Exit
                logout();
                break;
              }
              default: {
                invalidChoice(inputStr);
                break;
              }
            }
            break;
          }
          case ADMINMENU: { // Administrator Main Menu
            switch(inputInt) {
              case 1: { // Arbitrary Customer Account Summary
                String cusID;
                String firstPrompt = "Please enter the Customer ID: ";
                System.out.print(firstPrompt);
                cusID = scan.nextLine().trim();
                bankingSystem.accountSummary(cusID);
                break;
              }
              case 2: { //  Report A :: Customer Information with Total Balance in Decreasing Order
                bankingSystem.reportA();
                break;
              }
              case 3: { // Report B :: Find the Average Total Balance Between Age Groups
                String min, max;
                String firstPrompt = "Please enter the Minimum Age: ";
                System.out.print(firstPrompt);
                min = scan.nextLine().trim();
                System.out.format("%" + firstPrompt.length() + "s", "Maximum Age: ");
                max = scan.nextLine().trim();
                bankingSystem.reportB(min, max);
                break;
              }
              case 4: { 
                logout();
                break;
              }
              default: {
                invalidChoice(inputStr);
                break;
              }
            }
            break;
          }
        }
        if(errorStatus) System.out.println("Please try again.\n");
        else System.out.println();
      } catch(Exception err) {
        invalidChoice(inputStr);
      }
    }

    System.out.println("Thanks for banking with us. Bye!\n");
  }
 }