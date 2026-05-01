package com.lockbox.ui;

import com.lockbox.db.VaultDAO;
import com.lockbox.security.KeyDerivation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private final JPasswordField txtPassword;
    private final JButton btnLogin;
    private final JLabel lblStatus;
    private int failedAttempts = 0;
    private final VaultDAO vaultDAO;
    private byte[] derivedKey;

    public LoginFrame() {
        super("LockBox - Secure Login");
        vaultDAO = new VaultDAO();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Header
        JLabel lblHeader = new JLabel("Welcome to LockBox");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeader.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(lblHeader, gbc);

        JLabel lblSubHeader = new JLabel("Enter your master password to unlock");
        lblSubHeader.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubHeader.setForeground(Color.GRAY);
        lblSubHeader.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        mainPanel.add(lblSubHeader, gbc);

        // Password Field
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        txtPassword = new JPasswordField(20);
        txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
        txtPassword.putClientProperty("JTextField.placeholderText", "Master Password");
        txtPassword.putClientProperty("JTextField.showClearButton", true);
        mainPanel.add(txtPassword, gbc);

        // Status Label
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(new Color(255, 100, 100));
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 10, 10, 10);
        mainPanel.add(lblStatus, gbc);

        // Login Button
        btnLogin = new JButton("Unlock Vault");
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> attemptLogin());
        gbc.gridy = 4;
        gbc.ipady = 10;
        mainPanel.add(btnLogin, gbc);

        add(mainPanel, BorderLayout.CENTER);

        setSize(450, 400);
        setLocationRelativeTo(null);
        
        // Window Behavior
        getRootPane().setDefaultButton(btnLogin);
    }

    private void attemptLogin() {
        char[] password = txtPassword.getPassword();
        if (password.length == 0) {
            lblStatus.setText("Password cannot be empty");
            return;
        }

        try {
            byte[] storedSalt = vaultDAO.getConfigValue("salt");
            byte[] storedHash = vaultDAO.getConfigValue("hash");

            if (storedSalt == null || storedHash == null) {
                // First Run: Registration
                byte[] newSalt = KeyDerivation.generateSalt();
                byte[] newHash = KeyDerivation.deriveKey(password, newSalt);
                vaultDAO.setConfigValue("salt", newSalt);
                vaultDAO.setConfigValue("hash", newHash);

                derivedKey = KeyDerivation.deriveKey(password, newSalt);
                Arrays.fill(password, '0');
                openDashboard();
            } else {
                // Login
                if (KeyDerivation.verifyMasterPassword(password, storedSalt, storedHash)) {
                    derivedKey = KeyDerivation.deriveKey(password, storedSalt);
                    Arrays.fill(password, '0');
                    openDashboard();
                } else {
                    failedAttempts++;
                    handleFailedAttempt();
                }
            }
        } catch (Exception ex) {
            lblStatus.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleFailedAttempt() {
        if (failedAttempts >= 5) {
            int delaySeconds = (int) Math.pow(2, failedAttempts - 5);
            btnLogin.setEnabled(false);
            lblStatus.setText("Locked out for " + delaySeconds + " seconds.");

            Timer timer = new Timer(delaySeconds * 1000, e -> {
                btnLogin.setEnabled(true);
                lblStatus.setText(" ");
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            lblStatus.setText("Invalid Master Password. Attempts: " + failedAttempts);
        }
    }

    private void openDashboard() {
        DashboardFrame dashboardFrame = new DashboardFrame(this, derivedKey);
        dashboardFrame.setVisible(true);
        this.setVisible(false);
    }
}
