package com.lockbox.ui;

import javax.swing.*;
import java.awt.*;
import java.security.SecureRandom;

public class PasswordGeneratorDialog extends JDialog {
    private JCheckBox cbUpper, cbNumbers, cbSymbols;
    private JSpinner spinLength;
    private JTextField txtResult;

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?/";

    public PasswordGeneratorDialog(JFrame parent) {
        super(parent, "Password Generator", true);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlOptions = new JPanel(new GridLayout(4, 2));
        pnlOptions.add(new JLabel("Length:"));
        spinLength = new JSpinner(new SpinnerNumberModel(16, 8, 128, 1));
        pnlOptions.add(spinLength);

        cbUpper = new JCheckBox("Uppercase", true);
        pnlOptions.add(cbUpper);
        cbNumbers = new JCheckBox("Numbers", true);
        pnlOptions.add(cbNumbers);
        cbSymbols = new JCheckBox("Symbols", true);
        pnlOptions.add(cbSymbols);

        add(pnlOptions, BorderLayout.NORTH);

        txtResult = new JTextField();
        txtResult.setEditable(false);
        add(txtResult, BorderLayout.CENTER);

        JPanel pnlBtns = new JPanel();
        JButton btnGen = new JButton("Generate");
        btnGen.addActionListener(e -> generate());
        pnlBtns.add(btnGen);

        JButton btnCopy = new JButton("Copy");
        btnCopy.addActionListener(e -> {
            ClipboardManager.copyToClipboard(txtResult.getText());
            JOptionPane.showMessageDialog(this, "Copied to clipboard! Clears in 30s.");
        });
        pnlBtns.add(btnCopy);

        add(pnlBtns, BorderLayout.SOUTH);

        setSize(300, 200);
        setLocationRelativeTo(parent);
    }

    private void generate() {
        int length = (int) spinLength.getValue();
        StringBuilder pool = new StringBuilder(LOWER);
        if (cbUpper.isSelected())
            pool.append(UPPER);
        if (cbNumbers.isSelected())
            pool.append(DIGITS);
        if (cbSymbols.isSelected())
            pool.append(SYMBOLS);

        SecureRandom random = new SecureRandom();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < length; i++) {
            res.append(pool.charAt(random.nextInt(pool.length())));
        }
        txtResult.setText(res.toString());
    }
}
