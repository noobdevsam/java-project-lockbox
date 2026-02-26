package com.lockbox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final String DB_DIR = System.getProperty("user.home") + "/.lockbox";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/vault.db";

    public static void initializeDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create vault table
            String createVaultTable = """
                    CREATE TABLE IF NOT EXISTS vault (
                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                     site_name TEXT NOT NULL,
                     username TEXT NOT NULL,
                     password_blob BLOB NOT NULL,
                     iv BLOB NOT NULL
                    );""";
            stmt.execute(createVaultTable);

            // Create config table for Master Password Hash and Salt
            String createConfigTable = """
                    CREATE TABLE IF NOT EXISTS config (
                     key TEXT PRIMARY KEY,
                     value BLOB NOT NULL
                    );""";
            stmt.execute(createConfigTable);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
