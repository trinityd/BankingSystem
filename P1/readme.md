![image](https://user-images.githubusercontent.com/23157710/147059623-082f8854-177e-43df-bd10-bc08c697cee6.png)


I did the extra credit for Project 1 by implementing a GUI version, found in p1GUI.java. It assumes you have your own copy of ProgramLauncher/BatchInputProcessor in the same folder, have correctly set up your db.properties file, and ran p1_create.sql in your db2 environment beforehand so the schema is set up.

p1GUI uses the JavaFX library. 
JavaFX is supposedly built in for Java 8 and 9: the exact Java version I used to develop was Java 9.0.4. Note: it was REMOVED from JDK 11 (https://www.infoworld.com/article/3261066/javafx-will-be-removed-from-the-java-jdk.html).
So, if you can't switch to JDK 8/9, or if you're using JDK 8/9 and it's still not working by default for some reason, here's the install page (https://openjfx.io/openjfx-docs/#install-javafx) and instructions for adding it to your path (https://openjfx.io/openjfx-docs/#install-javafx). As a last resort, I've included the library in a folder: javafx-sdk-14.0.2.1. But hopefully you won't need to worry about it if you can just use Java 9.

p1GUI can be run much like the no-extra-credit method but with p1GUI instead of p1/ProgramLauncher, requiring the same db2 jar and properties file, with a call like the following on Windows Terminal:
javac *.java; java -cp ";./db2jcc4.jar" p1GUI ./db.properties;
and I *think* the following (colon instead of semicolon) on Mac, but I have no way to check:
javac *.java; java -cp ":./db2jcc4.jar" p1GUI ./db.properties;

p1GUI is fairly self explanatory - it directly follows the assignment spec. The secret admin login/password is 0/0. Click on the button corresponding to the choice you want to make, output will display below the menu, if a choice requires user input popup boxes will appear and you can enter your input there. It should handle all forms of bad input! (Besides SQL injection, which Prof. Butt said we didn't need to account for here.)


I also included the command line UI only version: p1.java. This can be run with no library dependencies (besides the normal db2 jar) with the following call (on Windows Terminal):
javac *.java; java -cp ";./db2jcc4.jar" p1 ./db.properties;


To load the test data and run the test commands, the normal  ProgramLauncher command works, i.e., 
javac *.java; java -cp ";./db2jcc4.jar" ProgramLauncher ./db.properties;
will produce the exact same output as test_full.out if done on a fresh DB (run p1_create.sql beforehand).
Running p1/p1GUI does NOT run the test commands in db.properties. I did it this way so the user could start from scratch by just running p1/p1GUI, or start with the test data by doing a ProgramLauncher call before the p1/p1GUI call if they want. 


I included a screenshot of what p1GUI looks like on Windows (BankingSystemScreenshot.png) if there's any visual issues that crop up on another OS - I don't have access to a Mac to test on there.
