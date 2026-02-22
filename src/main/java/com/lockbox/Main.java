package com.lockbox;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.FlatDarkLaf;
import com.lockbox.db.DatabaseHelper;

public class Main {
    public static void main(String[] args) {
        // Setup FlatLaf Dark Theme
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        // Initialize Database
        DatabaseHelper.initializeDatabase();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            new com.lockbox.ui.LoginFrame().setVisible(true);
        });
    }
}
