import javafx.application.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import java.util.Scanner;

/**
 * GUI for BankingSystem
 * Requires the javafx library to run
 * Must be run with db2jcc4.jar in same folder
 * args(0): db.properties file containing db login info in same folder 
 * Sample run on Windows Terminal: javac *.java; java -cp ";./db2jcc4.jar" p1GUI ./db.properties;
 */

public class p1GUI extends Application {
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
		Application.launch(args);
	}

	public static void logout() {
		bankingSystem.loggedInCustomerID = bankingSystem.nonLoggedInCustomerID;
		bankingSystem.loggedInCustomerName = bankingSystem.nonLoggedInCustomerName;
	}

	// GUI
	@Override
	public void start(Stage stage) {
		VBox mainMenuDisplay = new VBox();
		VBox customerMenuDisplay = new VBox();
		VBox adminMenuDisplay = new VBox();

		Text mainMenuResponseText = new Text();
		Text customerMenuResponseText = new Text();
		Text customerMenuLoginText = new Text();
		Text adminMenuResponseText = new Text();
		mainMenuResponseText.setStyle("-fx-font-family: 'monospaced';");
		customerMenuResponseText.setStyle("-fx-font-family: 'monospaced';");
		adminMenuResponseText.setStyle("-fx-font-family: 'monospaced';");

		// stage.getScene().setRoot(customerMenuDisplay);
		// stage.getScene().setRoot(adminMenuDisplay);

		// Main Menu
		mainMenuDisplay.setPadding(new Insets(5, 30, 10, 30));
		mainMenuDisplay.setAlignment(Pos.TOP_CENTER);
		// mainMenuDisplay.setFillWidth(true);
		Text mainMenuTitle = new Text(menus[MAINMENU][0]);
		mainMenuDisplay.getChildren().add(mainMenuTitle);
		Button mainMenu1 = new Button(menus[MAINMENU][1]);
		mainMenu1.setOnAction(event -> { // New Customer
			TextInputDialog td = new TextInputDialog();

			String name, gender, age, pin;
			String prompt = "Please enter the new account holder's name: ";
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			name = td.getEditor().getText().trim();
			prompt = "Please enter the new account holder's gender (M/F): ";
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			gender = td.getEditor().getText().trim();
			prompt = "Please enter the new account holder's age: ";
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			age = td.getEditor().getText().trim();
			prompt = "Please enter the new account holder's PIN: ";
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			pin = td.getEditor().getText().trim();

			bankingSystem.newCustomer(name, gender, age, pin);
			if(!bankingSystem.errorStatus) {
				mainMenuResponseText.setText(String.format("Created new customer with name: %s and ID: %s", name, bankingSystem.returnMsg));
			} else {
				mainMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button mainMenu2 = new Button(menus[MAINMENU][2]);
		mainMenu2.setOnAction(event -> { // Customer Login
			String cusID, pin;
			String prompt = "Please enter your Customer ID: ";
			TextInputDialog td = new TextInputDialog();
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			cusID = td.getEditor().getText().trim();
			prompt = "Please enter your PIN: ";
			td = new TextInputDialog();
			td.setHeaderText(prompt);
			td.showAndWait();
			pin = td.getEditor().getText().trim();

			if(cusID.equals(adminID) && pin.equals(adminPIN)) {
				bankingSystem.loggedInCustomerID = adminID;
				bankingSystem.loggedInCustomerName = adminName;
				stage.getScene().setRoot(adminMenuDisplay);
			} else {
				bankingSystem.login(cusID, pin);
				if(!bankingSystem.errorStatus) {
					bankingSystem.loggedInCustomerName = bankingSystem.returnMsg;
					bankingSystem.loggedInCustomerID = cusID;
					stage.getScene().setRoot(customerMenuDisplay);
					mainMenuResponseText.setText("");
					customerMenuLoginText.setText("Logged in as: " + bankingSystem.loggedInCustomerName + " with ID: " + bankingSystem.loggedInCustomerID);
				} else {
					mainMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
				}
			}
		});
		Button mainMenu3 = new Button(menus[MAINMENU][3]);
		mainMenu3.setOnAction(event -> { // Exit
			stage.close();
		});
		Button[] mainButtons = {mainMenu1, mainMenu2, mainMenu3};
		for(Button button : mainButtons) {
			mainMenuDisplay.getChildren().add(button);
		}
		mainMenuDisplay.getChildren().add(mainMenuResponseText);

		// Customer Menu
		customerMenuDisplay.setPadding(new Insets(5, 30, 10, 30));
		customerMenuDisplay.setAlignment(Pos.TOP_CENTER);
		// customerMenuDisplay.setFillWidth(true);
		Text customerMenuTitle = new Text(menus[CUSTOMERMENU][0]);
		customerMenuDisplay.getChildren().add(customerMenuTitle);
		Button customerMenu1 = new Button(menus[CUSTOMERMENU][1]);
		customerMenu1.setOnAction(event -> { // New Account
			String cusID, accType, balance;
			TextInputDialog td = new TextInputDialog();
			String prompt = "Please enter the new account holder's customer ID: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			cusID = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the new account's type (C for Checking, S for Savings): ";
			td.setHeaderText(prompt);
			td.showAndWait();
			accType = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the new account's starting balance: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			balance = td.getEditor().getText().trim();

			bankingSystem.openAccount(cusID, accType, balance);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(String.format("Opened new account #%s", bankingSystem.returnMsg));
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu2 = new Button(menus[CUSTOMERMENU][2]);
		customerMenu2.setOnAction(event -> { // Close Account
			String accNum;
			TextInputDialog td = new TextInputDialog();
			String prompt = "Please enter the account's number: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			accNum = td.getEditor().getText().trim();

			bankingSystem.closeAccount(accNum);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(String.format("Closed account #%s", accNum));
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu3 = new Button(menus[CUSTOMERMENU][3]);
		customerMenu3.setOnAction(event -> { // Deposit
			String accNum, amount;
			String prompt;
			TextInputDialog td = new TextInputDialog();
			prompt = "Please enter the account's number: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			accNum = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the amount to deposit: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			amount = td.getEditor().getText().trim();

			bankingSystem.deposit(accNum, amount);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(String.format("Deposited $%s into account #%s", amount, accNum));
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu4 = new Button(menus[CUSTOMERMENU][4]);
		customerMenu4.setOnAction(event -> { // Withdraw
			String accNum, amount;
			String prompt;
			TextInputDialog td = new TextInputDialog();
			prompt = "Please enter the account's number: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			accNum = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the amount to withdraw: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			amount = td.getEditor().getText().trim();

			bankingSystem.withdraw(accNum, amount);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(String.format("Withdrew $%s from account #%s", amount, accNum));
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu5 = new Button(menus[CUSTOMERMENU][5]);
		customerMenu5.setOnAction(event -> { // Transfer
			String srcAccNum, destAccNum, amount;
			String prompt;
			TextInputDialog td = new TextInputDialog();
			prompt = "Please enter the source account's number: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			srcAccNum = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the destination account's number: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			destAccNum = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the amount to withdraw: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			amount = td.getEditor().getText().trim();

			bankingSystem.transfer(srcAccNum, destAccNum, amount);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(String.format("Transferred $%s from account #%s to account #%s", amount, srcAccNum, destAccNum));
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu6 = new Button(menus[CUSTOMERMENU][6]);
		customerMenu6.setOnAction(event -> { // Logged in Account's Summary
			bankingSystem.accountSummary(bankingSystem.loggedInCustomerID);
			if(!bankingSystem.errorStatus) {
				customerMenuResponseText.setText(bankingSystem.returnMsg);
			} else {
				customerMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button customerMenu7 = new Button(menus[CUSTOMERMENU][7]);
		customerMenu7.setOnAction(event -> { // Exit
			logout();
			mainMenuResponseText.setText("Logged out.");
			stage.getScene().setRoot(mainMenuDisplay);
		});
		Button[] customerButtons = {customerMenu1, customerMenu2, customerMenu3, customerMenu4, customerMenu5, customerMenu6, customerMenu7};
		for(Button button : customerButtons) {
			customerMenuDisplay.getChildren().add(button);
		}
		customerMenuDisplay.getChildren().add(customerMenuLoginText);
		customerMenuDisplay.getChildren().add(customerMenuResponseText);

		// Admin Menu
		adminMenuDisplay.setPadding(new Insets(5, 30, 10, 30));
		adminMenuDisplay.setAlignment(Pos.TOP_CENTER);
		// adminMenuDisplay.setFillWidth(true);
		Text adminMenuTitle = new Text(menus[ADMINMENU][0]);
		adminMenuDisplay.getChildren().add(adminMenuTitle);
		Button adminMenu1 = new Button(menus[ADMINMENU][1]);
		adminMenu1.setOnAction(event -> { // Arbitrary Customer Account Summary
			String cusID;
			TextInputDialog td = new TextInputDialog();
			String prompt = "Please enter the Customer ID: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			cusID = td.getEditor().getText().trim();

			bankingSystem.accountSummary(cusID);
			if(!bankingSystem.errorStatus) {
				adminMenuResponseText.setText(bankingSystem.returnMsg);
			} else {
				adminMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button adminMenu2 = new Button(menus[ADMINMENU][2]);
		adminMenu2.setOnAction(event -> { //  Report A :: Customer Information with Total Balance in Decreasing Order
			bankingSystem.reportA();
			if(!bankingSystem.errorStatus) {
				adminMenuResponseText.setText(bankingSystem.returnMsg);
			} else {
				adminMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button adminMenu3 = new Button(menus[ADMINMENU][3]);
		adminMenu3.setOnAction(event -> { // Report B :: Find the Average Total Balance Between Age Groups
			String min, max;
			TextInputDialog td = new TextInputDialog();
			String prompt = "Please enter the Minimum Age: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			min = td.getEditor().getText().trim();
			td = new TextInputDialog();
			prompt = "Please enter the Minimum Age: ";
			td.setHeaderText(prompt);
			td.showAndWait();
			max = td.getEditor().getText().trim();

			bankingSystem.reportB(min, max);
			if(!bankingSystem.errorStatus) {
				adminMenuResponseText.setText(bankingSystem.returnMsg);
			} else {
				adminMenuResponseText.setText(String.format("Error: %s. Please try again.", bankingSystem.returnMsg));
			}
		});
		Button adminMenu4 = new Button(menus[ADMINMENU][4]);
		adminMenu4.setOnAction(event -> { // Exit
			logout();
			mainMenuResponseText.setText("Logged out.");
			stage.getScene().setRoot(mainMenuDisplay);
		});
		Button[] adminButtons = {adminMenu1, adminMenu2, adminMenu3, adminMenu4};
		for(Button button : adminButtons) {
			adminMenuDisplay.getChildren().add(button);
		}
		adminMenuDisplay.getChildren().add(adminMenuResponseText);

		Scene scene = new Scene(mainMenuDisplay);
		stage.setScene(scene);
		// stage.setX(200);
		// stage.setY(200);
		// stage.setMinHeight(480);
		// stage.setMinWidth(500);
		stage.setHeight(500);
		stage.setWidth(600);
		stage.setTitle("Self Services Banking System");

		mainMenuDisplay.setSpacing(10);
		mainMenuDisplay.prefWidthProperty().bind(stage.widthProperty().multiply(0.80));
		customerMenuDisplay.setSpacing(10);
		customerMenuDisplay.prefWidthProperty().bind(stage.widthProperty().multiply(0.80));
		adminMenuDisplay.setSpacing(10);
		adminMenuDisplay.prefWidthProperty().bind(stage.widthProperty().multiply(0.80));

		stage.show();
	}  
}