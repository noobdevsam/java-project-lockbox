package com.lockbox;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.lockbox.db.DatabaseHelper;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // Setup FlatLaf Dark Theme with Accent Color and advanced styling
        try {
            // Set the desired accent color using extra defaults (safer for version compatibility)
            Map<String, String> extras = new HashMap<>();
            extras.put("@accentColor", "#4285F4");
            FlatLaf.setGlobalExtraDefaults(extras);

            // Apply specific UI defaults using UIManager.put
            // General component styling
            UIManager.put("Component.arc", 10);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("Component.borderColor", Color.decode("#44475A"));
            UIManager.put("Component.focusColor", Color.decode("#4285F4"));

            // Table styling
            UIManager.put("Table.background", Color.decode("#282C34"));
            UIManager.put("Table.foreground", Color.WHITE);
            UIManager.put("Table.gridColor", Color.decode("#44475A"));
            UIManager.put("Table.selectionBackground", Color.decode("#4285F4"));
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("TableHeader.background", Color.decode("#282C34"));
            UIManager.put("TableHeader.foreground", Color.WHITE);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));

            // Button styling
            UIManager.put("Button.arc", 10);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("Button.selectedBackground", Color.decode("#4285F4"));

            // Text field styling
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("PasswordField.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));

            // Setup the theme
            UIManager.setLookAndFeel(new FlatDarkLaf());

        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF with advanced styling");
            ex.printStackTrace();
        }

        // Initialize Database
        DatabaseHelper.initializeDatabase();

        // Launch UI
        SwingUtilities.invokeLater(() -> {
            new com.lockbox.ui.LoginFrame().setVisible(true);
        });
    }
}
