This project 2 implementation uses a CLI only. It requires the user to have the db2 jar and properly set up db.properties in the same folder, same as project 1. It assumes a running database with your p2_create.sql + my p2.sql ran inside.
It wasn't clear in the instructions if we were supposed to add a UI option for ADD_INTEREST. I put it in the admin menu (accessible with 0/0 id/PIN).
The methods that involve printing things (Account Summary/Admin Account Summary/Report A/Report B) are left as normal JDBC - again it isn't clear in the doc whether we should delete them or what, but they aren't specified in the the stored procedures list so I assumed it was okay to leave them as they were in project 1.

To run p2, use the same command as p1 with p2 instead, for example (Windows Terminal):
javac *.java; java -cp ";./db2jcc4.jar" p2 ./db.properties;