package com.lockbox.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.security.SecureRandom;

public class PasswordGeneratorDialog extends JDialog {
    private final JCheckBox cbUpper;
    private final JCheckBox cbNumbers;
    private final JCheckBox cbSymbols;
    private final JSpinner spinLength;
    private final JTextField txtResult;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?/";

    public PasswordGeneratorDialog(JFrame parent) {
        super(parent, "Password Generator", true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Result Field
        txtResult = new JTextField(20);
        txtResult.setEditable(false);
        txtResult.setHorizontalAlignment(SwingConstants.CENTER);
        txtResult.setFont(new Font("Monospaced", Font.BOLD, 18));
        txtResult.setPreferredSize(new Dimension(0, 50));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(txtResult, gbc);

        // Options
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password Length:"), gbc);
        spinLength = new JSpinner(new SpinnerNumberModel(16, 8, 128, 1));
        gbc.gridx = 1;
        mainPanel.add(spinLength, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        cbUpper = new JCheckBox("Include Uppercase", true);
        mainPanel.add(cbUpper, gbc);

        gbc.gridx = 1;
        cbNumbers = new JCheckBox("Include Numbers", true);
        mainPanel.add(cbNumbers, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        cbSymbols = new JCheckBox("Include Symbols", true);
        mainPanel.add(cbSymbols, gbc);

        // Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JButton btnGen = new JButton("Generate");
        btnGen.addActionListener(e -> generate());
        pnlBtns.add(btnGen);

        JButton btnCopy = new JButton("Copy Password");
        btnCopy.addActionListener(e -> {
            if (!txtResult.getText().isEmpty()) {
                ClipboardManager.copyToClipboard(txtResult.getText());
                JOptionPane.showMessageDialog(this, "Copied to clipboard!");
            }
        });
        pnlBtns.add(btnCopy);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.ipady = 10;
        gbc.insets = new Insets(20, 10, 10, 10);
        mainPanel.add(pnlBtns, gbc);

        add(mainPanel, BorderLayout.CENTER);

        pack();
        setSize(550, 450);
        setLocationRelativeTo(parent);
        
        generate(); // Generate an initial password
    }

    private void generate() {
        int length = (int) spinLength.getValue();
        StringBuilder pool = new StringBuilder(LOWER);
        if (cbUpper.isSelected()) pool.append(UPPER);
        if (cbNumbers.isSelected()) pool.append(DIGITS);
        if (cbSymbols.isSelected()) pool.append(SYMBOLS);

        SecureRandom random = new SecureRandom();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; i++) {
            res.append(pool.charAt(random.nextInt(pool.length())));
        }
        txtResult.setText(res.toString());
    }
}
