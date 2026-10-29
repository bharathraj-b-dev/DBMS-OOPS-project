CINEBOOK - JAVA OOP + MYSQL DBMS PROJECT
========================================

WHAT THIS PROJECT USES
- Java classes and objects
- Encapsulation
- Interface
- Enum with ticket prices
- 2D array for the seat map
- ArrayList
- Method overloading
- Static booking ID counter
- MySQL tables, primary keys, foreign keys, SELECT, INSERT and UPDATE
- JDBC to connect Java to MySQL

REQUIREMENTS
1. JDK 17 or newer
2. MySQL Server
3. MySQL Connector/J JAR
4. Terminal / Command Prompt

STEP 1: CREATE THE DATABASE
1. Open MySQL Workbench.
2. Open schema.sql.
3. Execute the entire script.
   It creates the cinebook database, tables, three shows and 120 seats.

STEP 2: SET YOUR MYSQL PASSWORD
Open DBConnection.java.
Replace:
    YOUR_MYSQL_PASSWORD
with the password for your MySQL root account.
If your MySQL account has no password, use an empty string: "".

STEP 3: GET MYSQL CONNECTOR/J
Download MySQL Connector/J from the official MySQL website:
https://dev.mysql.com/downloads/connector/j/
Choose the platform-independent ZIP, extract it, and find the file named
mysql-connector-j-...jar.

STEP 4: COMPILE AND RUN (WINDOWS)
Put all .java files and the connector JAR in the same folder.
Open Command Prompt in that folder.

Compile:
    javac -cp ".;mysql-connector-j-9.x.x.jar" *.java

Replace mysql-connector-j-9.x.x.jar with the exact JAR filename you downloaded.

Run:
    java -cp ".;mysql-connector-j-9.x.x.jar" CineApp

Use the same exact JAR filename in both commands.

If using macOS/Linux, replace the semicolon in the classpath with a colon.

STEP 5: TRY THE APP
1. Choose 1 to view shows and book tickets.
2. Select show ID 1, 2 or 3.
3. Choose seats like C3,C4.
4. Enter CINE10 for a 10% discount, or press Enter to skip.
5. Note the booking ID.
6. Choose 2 to cancel. Enter the show ID and booking ID.
7. Choose 3 to view booking records.
8. Choose 4 to exit.

SEAT PRICES
Rows A-B: Regular, Rs. 150 each
Rows C-D: Premium, Rs. 250 each
Row E: VIP, Rs. 400 each

NOTES
- A booking is all-or-nothing: all seats are checked before any seat is updated.
- Booking records remain in MySQL after the Java program closes.
- Cancelling a booking marks it CANCELLED and makes its seats available again.
- The refund is 80% of the amount stored for the booking.
- The app is console-based; it has no GUI.
- This is a sample college project, not a production payment system.

TROUBLESHOOTING
- "Access denied": check username/password in DBConnection.java.
- "Unknown database cinebook": run schema.sql in MySQL Workbench.
- "No suitable driver": check the Connector/J JAR is in the classpath and filename is correct.
- "Communications link failure": make sure MySQL Server is running.
