package com.lockbox.ui;

import com.lockbox.db.VaultDAO;
import com.lockbox.db.VaultEntry;
import com.lockbox.security.CryptoUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final JFrame parentLogin;
    private final byte[] masterKey;
    private final VaultDAO vaultDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<VaultEntry> currentEntries;

    public DashboardFrame(JFrame parentLogin, byte[] masterKey) {
        super("LockBox - Dashboard");
        this.parentLogin = parentLogin;
        this.masterKey = masterKey;
        this.vaultDAO = new VaultDAO();

        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        // Top Panel: Actions
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        JButton btnAdd = new JButton("Add Entry");
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.addActionListener(e -> showAddEditDialog(null));
        JButton btnGen = new JButton("Password Generator");
        btnGen.putClientProperty("JButton.buttonType", "roundRect");
        btnGen.addActionListener(e -> new PasswordGeneratorDialog(this).setVisible(true));
        JButton btnLogout = new JButton("Logout");
        btnLogout.putClientProperty("JButton.buttonType", "roundRect");
        btnLogout.addActionListener(e -> logout());

        pnlTop.add(btnAdd);
        pnlTop.add(btnGen);
        pnlTop.add(btnLogout);
        add(pnlTop, BorderLayout.NORTH);

        // Center Panel: Table
        tableModel = new DefaultTableModel(new String[]{"Site", "Username", "Password"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel: Context Actions
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        JButton btnCopy = new JButton("Copy Password");
        btnCopy.putClientProperty("JButton.buttonType", "roundRect");
        btnCopy.addActionListener(e -> copySelectedPassword());
        JButton btnView = new JButton("View Password");
        btnView.putClientProperty("JButton.buttonType", "roundRect");
        btnView.addActionListener(e -> viewSelectedPassword());
        JButton btnEdit = new JButton("Edit");
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnEdit.addActionListener(e -> editSelectedEntry());
        JButton btnDelete = new JButton("Delete");
        btnDelete.putClientProperty("JButton.buttonType", "roundRect");
        btnDelete.setForeground(new Color(255, 100, 100));
        btnDelete.addActionListener(e -> deleteSelectedEntry());

        pnlBottom.add(btnCopy);
        pnlBottom.add(btnView);
        pnlBottom.add(btnEdit);
        pnlBottom.add(btnDelete);
        add(pnlBottom, BorderLayout.SOUTH);

        setSize(900, 600);
        setLocationRelativeTo(null);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            currentEntries = vaultDAO.getAllEntries();
            for (VaultEntry entry : currentEntries) {
                String b64User = entry.getUsername();
                String decUser = "Error";
                try {
                    byte[] userPayload = java.util.Base64.getDecoder().decode(b64User);
                    byte[] userIv = Arrays.copyOfRange(userPayload, 0, 12);
                    byte[] userCipher = Arrays.copyOfRange(userPayload, 12, userPayload.length);
                    decUser = new String(CryptoUtil.decrypt(userCipher, masterKey, userIv), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    decUser = "Decryption Failed";
                }
                tableModel.addRow(new Object[] { entry.getId(), entry.getSiteName(), decUser, "••••••" });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading vault: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showAddEditDialog(VaultEntry entryToEdit) {
        JDialog dialog = new JDialog(this, entryToEdit == null ? "Add Entry" : "Edit Entry", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));

        dialog.add(new JLabel("Site Name:"));
        JTextField txtSite = new JTextField();
        dialog.add(txtSite);

        dialog.add(new JLabel("Username:"));
        JTextField txtUser = new JTextField();
        dialog.add(txtUser);

        dialog.add(new JLabel("Password:"));
        JPasswordField txtPass = new JPasswordField();
        dialog.add(txtPass);

        if (entryToEdit != null) {
            txtSite.setText(entryToEdit.getSiteName());
            try {
                byte[] userPayload = java.util.Base64.getDecoder().decode(entryToEdit.getUsername());
                byte[] userIv = Arrays.copyOfRange(userPayload, 0, 12);
                byte[] userCipher = Arrays.copyOfRange(userPayload, 12, userPayload.length);
                txtUser.setText(new String(CryptoUtil.decrypt(userCipher, masterKey, userIv), StandardCharsets.UTF_8));
                txtPass.setText(
                        new String(CryptoUtil.decrypt(entryToEdit.getPasswordBlob(), masterKey, entryToEdit.getIv()),
                                StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }

        JButton btnSave = new JButton("Save");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.addActionListener(e -> {
            try {
                byte[] passIv = CryptoUtil.generateIV();
                byte[] encPass = CryptoUtil.encrypt(new String(txtPass.getPassword()).getBytes(StandardCharsets.UTF_8),
                        masterKey, passIv);

                // Need username as String for DB
                byte[] userIv = CryptoUtil.generateIV();
                byte[] encUser = CryptoUtil.encrypt(txtUser.getText().getBytes(StandardCharsets.UTF_8), masterKey,
                        userIv);

                byte[] userPayload = new byte[userIv.length + encUser.length];
                System.arraycopy(userIv, 0, userPayload, 0, userIv.length);
                System.arraycopy(encUser, 0, userPayload, userIv.length, encUser.length);

                String b64User = java.util.Base64.getEncoder().encodeToString(userPayload);

                if (entryToEdit == null) {
                    vaultDAO.insertEntry(new VaultEntry(0, txtSite.getText(), b64User, encPass, passIv));
                } else {
                    entryToEdit.setSiteName(txtSite.getText());
                    entryToEdit.setUsername(b64User);
                    entryToEdit.setPasswordBlob(encPass);
                    entryToEdit.setIv(passIv);
                    vaultDAO.updateEntry(entryToEdit);
                }
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Encryption error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        dialog.add(new JLabel()); // spacer
        dialog.add(btnSave);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setSize(500, 200);
        dialog.setVisible(true);
    }

    private VaultEntry getSelectedEntry() {
        int row = table.getSelectedRow();
        if (row == -1)
            return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return currentEntries.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    private void editSelectedEntry() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            showAddEditDialog(entry);
        } else {
            JOptionPane.showMessageDialog(this, "Select an entry to edit.");
        }
    }

    private void deleteSelectedEntry() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this entry?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    vaultDAO.deleteEntry(entry.getId());
                    refreshTable();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Delete error: " + ex.getMessage());
                }
            }
        }
    }

    private void copySelectedPassword() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            try {
                String decrypted = new String(CryptoUtil.decrypt(entry.getPasswordBlob(), masterKey, entry.getIv()),
                        StandardCharsets.UTF_8);
                ClipboardManager.copyToClipboard(decrypted);
                JOptionPane.showMessageDialog(this, "Password copied to clipboard (clears in 30s).");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Decryption error: " + ex.getMessage());
            }
        }
    }

    private void viewSelectedPassword() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            try {
                String decrypted = new String(CryptoUtil.decrypt(entry.getPasswordBlob(), masterKey, entry.getIv()),
                        StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this, "Password: \n" + decrypted, "View Password",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Decryption error: " + ex.getMessage());
            }
        }
    }

    private void logout() {
        Arrays.fill(masterKey, (byte) 0);
        ClipboardManager.clearClipboard();
        this.dispose();
        parentLogin.setVisible(true);
        // Clear login password field manually if possible, or just instantiate new
        // LoginFrame.
    }
}
