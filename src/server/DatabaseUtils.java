// File: server/DatabaseUtils.java
package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtils {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/p2p_chat";
    private static final String DB_USER = "chat";
    private static final String DB_PASSWORD = "chat1234";

    // Method to get a database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
