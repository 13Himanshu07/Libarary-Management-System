# Library Management System (Java + Swing + MySQL)

A desktop Library Management System with a Swing GUI, backed by a MySQL
database over JDBC. Manage books and members, and issue/return books with
automatic late-fine calculation.

## Features

* **Books tab** — add, update, delete, and search books (by title, author,
category, or ISBN). Tracks total vs. available copies.
* **Members tab** — add, update, delete, and search members.
* **Issue / Return tab** — issue an available book to a member with a due
date, return a book (auto-calculates a late fine of ₹5/day if overdue),
and view either active issues or the full transaction history.
* **Modern UI** — dark sidebar navigation, styled buttons/tables, and a
[FlatLaf](https://www.formdev.com/flatlaf/) flat theme (falls back
gracefully to Nimbus if you skip that download — see step 2 below).

## Project structure

```
LibraryManagementSystem/
├── sql/
│   └── library\_db.sql        # run once to create the database + tables (+ sample data)
├── src/com/library/
│   ├── Main.java              # entry point — sets up the FlatLaf theme
│   ├── db/DBConnection.java   # JDBC connection settings — EDIT THIS
│   ├── model/                 # Book, Member, Transaction
│   ├── dao/                   # BookDAO, MemberDAO, TransactionDAO (all SQL lives here)
│   └── gui/                   # UITheme, MainFrame, BookPanel, MemberPanel, IssueReturnPanel
├── run.bat / run.sh           # double-click launcher — compiles + runs in one go
└── README.md
```

## Setup

### 1\. Create the database

Make sure MySQL is installed and running, then run:

```bash
mysql -u root -p < sql/library\_db.sql
```

This creates the `library\_db` database, its three tables (`books`,
`members`, `transactions`), and a few sample rows so the app isn't empty
the first time you launch it.

### 2\. Download two jars into `lib/`

Neither is bundled with this project — both are plain `.jar` files you drop
into the `lib/` folder:

|Jar|Where to get it|Required?|
|-|-|-|
|`mysql-connector-j-\*.jar`|https://dev.mysql.com/downloads/connector/j/ (choose **Platform Independent**, then unzip and grab the single `.jar` inside)|Yes — the app can't reach MySQL without it|
|`flatlaf-\*.jar`|https://www.formdev.com/flatlaf/#download (grab the "flatlaf-<version>.jar" core file — not the theme-pack or extras)|Optional — the modern flat theme. Skipping it just falls back to the built-in Nimbus look|

### 3\. Point the app at your database

Open `src/com/library/db/DBConnection.java` and edit these three lines to
match your MySQL setup:

```java
private static final String URL = "jdbc:mysql://localhost:3306/library\_db?useSSL=false\&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your\_password";
```

### 4\. Compile and run

**Easiest — double-click the launcher:** `run.bat` (Windows) or `run.sh`
(macOS/Linux, run `chmod +x run.sh` once first). It compiles and launches
in one step and automatically picks up every `.jar` in `lib/`.

**From the command line** (Linux/macOS — use `;` instead of `:` on Windows):

```bash
javac -d bin -cp "lib/\*" -sourcepath src src/com/library/Main.java
java -cp "bin:lib/\*" com.library.Main
```

**From an IDE (IntelliJ IDEA / Eclipse / NetBeans):**

1. Open the `LibraryManagementSystem` folder as a project.
2. Add both downloaded jars to the project's build path / external libraries.
3. Set `com.library.Main` as the run configuration's main class.
4. Run it.

## Notes

* The loan period defaults to 14 days and the late fine is ₹5/day — both
are constants (`DEFAULT\_LOAN\_DAYS`, `FINE\_PER\_DAY`) at the top of
`TransactionDAO.java` if you want to change them.
* Colors, fonts, and button/table styling all live in one place —
`src/com/library/gui/UITheme.java` — so you can retheme the whole app
by changing values there instead of hunting through every panel.
* All SQL is written with `PreparedStatement`, so it's safe from SQL
injection.
* The whole project was compiled locally against OpenJDK 21 with no errors
before being handed to you (java.sql/javax.swing are part of the JDK, so
everything compiles even without either jar on the classpath — both are
only needed at *runtime*).
