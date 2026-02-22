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
    private JFrame parentLogin;
    private byte[] masterKey;
    private VaultDAO vaultDAO;
    private DefaultTableModel tableModel;
    private JTable table;
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
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add Entry");
        btnAdd.addActionListener(e -> showAddEditDialog(null));
        JButton btnGen = new JButton("Password Generator");
        btnGen.addActionListener(e -> new PasswordGeneratorDialog(this).setVisible(true));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());

        pnlTop.add(btnAdd);
        pnlTop.add(btnGen);
        pnlTop.add(btnLogout);
        add(pnlTop, BorderLayout.NORTH);

        // Center Panel: Table
        tableModel = new DefaultTableModel(new String[] { "ID", "Site", "Username", "Password" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Panel: Context Actions
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCopy = new JButton("Copy Password");
        btnCopy.addActionListener(e -> copySelectedPassword());
        JButton btnView = new JButton("View Password");
        btnView.addActionListener(e -> viewSelectedPassword());
        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> editSelectedEntry());
        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteSelectedEntry());

        pnlBottom.add(btnCopy);
        pnlBottom.add(btnView);
        pnlBottom.add(btnEdit);
        pnlBottom.add(btnDelete);
        add(pnlBottom, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            currentEntries = vaultDAO.getAllEntries();
            for (VaultEntry entry : currentEntries) {
                String decUser = new String(CryptoUtil.decrypt(entry.getUsername().getBytes(StandardCharsets.UTF_8),
                        masterKey, entry.getIv()), StandardCharsets.UTF_8);
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
                txtUser.setText(
                        new String(CryptoUtil.decrypt(entryToEdit.getUsername().getBytes(StandardCharsets.UTF_8),
                                masterKey, entryToEdit.getIv()), StandardCharsets.UTF_8));
                txtPass.setText(
                        new String(CryptoUtil.decrypt(entryToEdit.getPasswordBlob(), masterKey, entryToEdit.getIv()),
                                StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> {
            try {
                byte[] iv = CryptoUtil.generateIV();
                byte[] encUser = CryptoUtil.encrypt(txtUser.getText().getBytes(StandardCharsets.UTF_8), masterKey, iv);
                byte[] encPass = CryptoUtil.encrypt(new String(txtPass.getPassword()).getBytes(StandardCharsets.UTF_8),
                        masterKey, iv);

                // Need username as String for DB
                // Wait, username encryption: let's store encrypted username as Base64 or Hex,
                // OR keep it text and just encrypt password?
                // Requirements: "username: TEXT (Encrypted)"
                String b64User = java.util.Base64.getEncoder().encodeToString(encUser);

                if (entryToEdit == null) {
                    vaultDAO.insertEntry(new VaultEntry(0, txtSite.getText(), b64User, encPass, iv));
                } else {
                    entryToEdit.setSiteName(txtSite.getText());
                    entryToEdit.setUsername(b64User);
                    entryToEdit.setPasswordBlob(encPass);
                    entryToEdit.setIv(iv);
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
