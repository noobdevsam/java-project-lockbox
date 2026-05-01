package com.lockbox.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // Adjusted DB_DIR to be more specific to the project if possible, or keep as is for user-wide
    // For now, keeping it as user-wide for simplicity as per original.
    private static final String DB_DIR = System.getProperty("user.home") + File.separator + ".lockbox";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + File.separator + "vault.db";

    public static void initializeDatabase() {
        File dir = new File(DB_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create vault table with new columns for notes, document, and password history
            String createVaultTable = """
                    CREATE TABLE IF NOT EXISTS vault (
                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                     site_name TEXT NOT NULL,
                     username TEXT NOT NULL,
                     password_blob BLOB NOT NULL,
                     iv BLOB NOT NULL,
                     secure_notes TEXT,
                     encrypted_document_content BLOB,
                     original_file_name TEXT,
                     password_history_blobs BLOB,
                     password_history_ivs BLOB
                    );""";
            stmt.execute(createVaultTable);

            // Create config table for Master Password Hash and Salt
            // This table remains unchanged
            String createConfigTable = """
                    CREATE TABLE IF NOT EXISTS config (
                     key TEXT PRIMARY KEY,
                     value BLOB NOT NULL
                    );""";
            stmt.execute(createConfigTable);

        } catch (SQLException e) {
            // Consider more robust error handling, e.g., logging or throwing a custom exception
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
