package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that hands out a single JDBC connection to the MySQL
 * "library_db" database. Edit URL / USER / PASSWORD below to match your
 * local MySQL setup before running the application.
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    private static Connection connection;

    private DBConnection() {
        // utility class - no instances
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "MySQL JDBC driver not found on the classpath. "
                                + "Download mysql-connector-j and add its .jar to lib/, "
                                + "then include it when compiling/running.", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
