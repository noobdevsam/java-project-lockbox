package com.lockbox.ui;

import com.lockbox.db.VaultDAO;
import com.lockbox.security.KeyDerivation;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblStatus;
    private int failedAttempts = 0;
    private VaultDAO vaultDAO;
    private byte[] derivedKey;

    public LoginFrame() {
        super("LockBox - Login");
        vaultDAO = new VaultDAO();

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel pnlCenter = new JPanel(new GridLayout(3, 1));

        JLabel lblPass = new JLabel("Master Password:", SwingConstants.CENTER);
        pnlCenter.add(lblPass);

        txtPassword = new JPasswordField(20);
        txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel pnlTxt = new JPanel();
        pnlTxt.add(txtPassword);
        pnlCenter.add(pnlTxt);

        btnLogin = new JButton("Login / Register");
        btnLogin.addActionListener(e -> attemptLogin());
        JPanel pnlBtn = new JPanel();
        pnlBtn.add(btnLogin);
        pnlCenter.add(pnlBtn);

        add(pnlCenter, BorderLayout.CENTER);

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        add(lblStatus, BorderLayout.SOUTH);

        setSize(400, 200);
        setLocationRelativeTo(null);
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
        this.setVisible(false); // Hide login
    }
}
