package com.lockbox.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.lockbox.db.VaultDAO;
import com.lockbox.db.VaultEntry;
import com.lockbox.security.CryptoUtil;

public class DashboardFrame extends JFrame {
    private final JFrame parentLogin;
    private final byte[] masterKey;
    private final VaultDAO vaultDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<VaultEntry> currentEntries;
    private JTextField txtSearch;

    // Details Panel Components
    private JPanel pnlDetails;
    private JPanel pnlPlaceholder;
    private JLabel lblDetTitle;
    private JTextField txtDetUser;
    private JPasswordField txtDetPass;
    private JTextArea txtDetNotes;
    private JButton btnDetDownloadDoc;
    private JButton btnDetHistory;

    public DashboardFrame(JFrame parentLogin, byte[] masterKey) {
        super("LockBox - Secure Vault v2.0");
        this.parentLogin = parentLogin;
        this.masterKey = masterKey;
        this.vaultDAO = new VaultDAO();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });

        setLayout(new BorderLayout());

        // Header Panel
        add(createHeader(), BorderLayout.NORTH);

        // Center Content: Split Pane
        tableModel = new DefaultTableModel(new String[]{"ID", "Service", "Username", "Extras"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getSelectionModel().addListSelectionListener(e -> updateDetailsPanel());

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel pnlCenter = new JPanel(new CardLayout());
        pnlCenter.add(createDetailsPanel(), "details");
        pnlCenter.add(createPlaceholderPanel(), "placeholder");
        ((CardLayout) pnlCenter.getLayout()).show(pnlCenter, "placeholder");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTable, pnlCenter);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(new EmptyBorder(10, 25, 10, 25));

        add(splitPane, BorderLayout.CENTER);

        // Bottom Panel
        add(createBottomPanel(), BorderLayout.SOUTH);

        setSize(1200, 800);
        setLocationRelativeTo(null);
        refreshTable();
    }

    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBorder(new EmptyBorder(20, 25, 10, 25));

        JLabel lblTitle = new JLabel("Your Vault");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search service or username...");
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterTable();
            }
        });

        JButton btnAdd = new JButton("Add New Entry");
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.setBackground(UIManager.getColor("Component.accentColor"));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());

        pnlActions.add(txtSearch);
        pnlActions.add(btnAdd);
        pnlActions.add(btnLogout);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        return pnlHeader;
    }

    private JPanel createPlaceholderPanel() {
        pnlPlaceholder = new JPanel(new GridBagLayout());
        JLabel lblHint = new JLabel("Select an entry to view details");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        lblHint.setForeground(Color.GRAY);
        pnlPlaceholder.add(lblHint);
        return pnlPlaceholder;
    }

    private JPanel createDetailsPanel() {
        pnlDetails = new JPanel(new GridBagLayout());
        pnlDetails.setBorder(new EmptyBorder(15, 20, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        lblDetTitle = new JLabel("Service Name");
        lblDetTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlDetails.add(lblDetTitle, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        pnlDetails.add(new JLabel("Username"), gbc);
        txtDetUser = new JTextField();
        txtDetUser.setEditable(false);
        txtDetUser.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 2;
        pnlDetails.add(txtDetUser, gbc);

        gbc.gridy = 3;
        pnlDetails.add(new JLabel("Password"), gbc);
        txtDetPass = new JPasswordField();
        txtDetPass.setEditable(false);
        txtDetPass.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridy = 4;
        pnlDetails.add(txtDetPass, gbc);

        JPanel pnlPassActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnShow = new JButton("Show/Hide");
        btnShow.addActionListener(e -> {
            if (txtDetPass.getEchoChar() == (char) 0) txtDetPass.setEchoChar('•');
            else txtDetPass.setEchoChar((char) 0);
        });
        JButton btnCopy = new JButton("Copy Password");
        btnCopy.addActionListener(e -> copySelectedPassword());
        pnlPassActions.add(btnShow);
        pnlPassActions.add(btnCopy);
        gbc.gridy = 5;
        pnlDetails.add(pnlPassActions, gbc);

        gbc.gridy = 6;
        pnlDetails.add(new JLabel("Secure Notes"), gbc);
        txtDetNotes = new JTextArea(10, 20);
        txtDetNotes.setEditable(false);
        txtDetNotes.setLineWrap(true);
        txtDetNotes.setWrapStyleWord(true);
        txtDetNotes.setBackground(UIManager.getColor("Table.background"));
        gbc.gridy = 7;
        pnlDetails.add(new JScrollPane(txtDetNotes), gbc);

        gbc.gridy = 8;
        pnlDetails.add(new JLabel("Vault Actions"), gbc);
        
        JPanel pnlExt = new JPanel(new GridLayout(1, 2, 15, 0));
        btnDetDownloadDoc = new JButton("Download Attachment");
        btnDetDownloadDoc.setToolTipText("Retrieve the encrypted document attached to this entry");
        btnDetDownloadDoc.addActionListener(e -> downloadAttachment());
        
        btnDetHistory = new JButton("Password History");
        btnDetHistory.setToolTipText("View and revert to previous passwords");
        btnDetHistory.addActionListener(e -> showHistoryDialog());
        
        pnlExt.add(btnDetDownloadDoc);
        pnlExt.add(btnDetHistory);
        gbc.gridy = 9;
        pnlDetails.add(pnlExt, gbc);

        gbc.gridy = 10; gbc.weighty = 1.0;
        pnlDetails.add(new JLabel(""), gbc); // Spacer

        return pnlDetails;
    }

    private JPanel createBottomPanel() {
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBorder(new EmptyBorder(15, 25, 25, 25));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        JButton btnGen = new JButton("Open Password Generator");
        btnGen.addActionListener(e -> new PasswordGeneratorDialog(this).setVisible(true));
        pnlLeft.add(btnGen);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        JButton btnEdit = new JButton("Edit Entry");
        btnEdit.addActionListener(e -> editSelectedEntry());
        JButton btnDelete = new JButton("Delete Entry");
        btnDelete.setForeground(new Color(255, 100, 100));
        btnDelete.addActionListener(e -> deleteSelectedEntry());
        pnlRight.add(btnEdit);
        pnlRight.add(btnDelete);

        pnlBottom.add(pnlLeft, BorderLayout.WEST);
        pnlBottom.add(pnlRight, BorderLayout.EAST);

        return pnlBottom;
    }

    private void updateDetailsPanel() {
        VaultEntry entry = getSelectedEntry();
        JPanel pnlParent = (JPanel) pnlDetails.getParent();
        CardLayout cl = (CardLayout) pnlParent.getLayout();
        
        if (entry == null) {
            cl.show(pnlParent, "placeholder");
        } else {
            cl.show(pnlParent, "details");
            lblDetTitle.setText(entry.getSiteName());
            txtDetUser.setText(decryptUsername(entry.getUsername()));
            try {
                txtDetPass.setText(new String(CryptoUtil.decrypt(entry.getPasswordBlob(), masterKey, entry.getIv()), StandardCharsets.UTF_8));
                txtDetPass.setEchoChar('•');
            } catch (Exception e) { txtDetPass.setText("ERROR"); }
            txtDetNotes.setText(entry.getSecureNotes());
            
            boolean hasDoc = entry.getEncryptedDocumentContent() != null && entry.getEncryptedDocumentContent().length > 0;
            btnDetDownloadDoc.setEnabled(hasDoc);
            btnDetDownloadDoc.setText(hasDoc ? "Download Attachment (" + (entry.getEncryptedDocumentContent().length / 1024) + " KB)" : "No Attachment");
            
            int historyCount = entry.getPasswordHistoryBlobs().size();
            btnDetHistory.setEnabled(historyCount > 0);
            btnDetHistory.setText("History (" + historyCount + ")");
        }
        pnlDetails.revalidate();
        pnlDetails.repaint();
    }

    private void filterTable() {
        String query = txtSearch.getText().toLowerCase();
        tableModel.setRowCount(0);
        for (VaultEntry entry : currentEntries) {
            String decUser = decryptUsername(entry.getUsername());
            if (entry.getSiteName().toLowerCase().contains(query) || decUser.toLowerCase().contains(query)) {
                String extras = "";
                if (entry.getEncryptedDocumentContent() != null && entry.getEncryptedDocumentContent().length > 0) extras += "📄 ";
                if (!entry.getPasswordHistoryBlobs().isEmpty()) extras += "🕒 (" + entry.getPasswordHistoryBlobs().size() + ")";
                tableModel.addRow(new Object[] { entry.getId(), entry.getSiteName(), decUser, extras });
            }
        }
    }

    private String decryptUsername(String b64User) {
        try {
            byte[] userPayload = java.util.Base64.getDecoder().decode(b64User);
            byte[] userIv = Arrays.copyOfRange(userPayload, 0, 12);
            byte[] userCipher = Arrays.copyOfRange(userPayload, 12, userPayload.length);
            return new String(CryptoUtil.decrypt(userCipher, masterKey, userIv), StandardCharsets.UTF_8);
        } catch (Exception e) { return "Error"; }
    }

    private void refreshTable() {
        try {
            currentEntries = vaultDAO.getAllEntries();
            filterTable();
            updateDetailsPanel();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void showAddEditDialog(VaultEntry entryToEdit) {
        JDialog dialog = new JDialog(this, entryToEdit == null ? "Add New Vault Entry" : "Edit Vault Entry", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 20, 12, 20);

        JTextField txtSite = new JTextField(30);
        JTextField txtUser = new JTextField(30);
        JPasswordField txtPass = new JPasswordField(30);
        JTextArea txtNotes = new JTextArea(5, 30);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        
        final byte[][] attachmentData = { entryToEdit != null ? entryToEdit.getEncryptedDocumentContent() : new byte[0] };
        JLabel lblAttach = new JLabel(attachmentData[0].length > 0 ? "Attached: " + (attachmentData[0].length/1024) + " KB" : "No file attached");

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1; dialog.add(txtSite, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(txtUser, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; dialog.add(txtPass, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(new JLabel("Secure Notes:"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(txtNotes), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; dialog.add(new JLabel("Attachment:"), gbc);
        JPanel pnlDoc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnAttach = new JButton("Select File");
        btnAttach.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    byte[] raw = Files.readAllBytes(fc.getSelectedFile().toPath());
                    byte[] iv = CryptoUtil.generateIV();
                    byte[] encrypted = CryptoUtil.encrypt(raw, masterKey, iv);
                    byte[] payload = new byte[iv.length + encrypted.length];
                    System.arraycopy(iv, 0, payload, 0, iv.length);
                    System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
                    attachmentData[0] = payload;
                    lblAttach.setText("Attached: " + fc.getSelectedFile().getName() + " (" + (payload.length/1024) + " KB)");
                } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Error attaching file: " + ex.getMessage()); }
            }
        });
        JButton btnClearDoc = new JButton("Clear");
        btnClearDoc.addActionListener(e -> {
            attachmentData[0] = new byte[0];
            lblAttach.setText("No file attached");
        });
        pnlDoc.add(btnAttach);
        pnlDoc.add(btnClearDoc);
        pnlDoc.add(lblAttach);
        gbc.gridx = 1; dialog.add(pnlDoc, gbc);

        if (entryToEdit != null) {
            txtSite.setText(entryToEdit.getSiteName());
            txtUser.setText(decryptUsername(entryToEdit.getUsername()));
            txtNotes.setText(entryToEdit.getSecureNotes());
            try { 
                txtPass.setText(new String(CryptoUtil.decrypt(entryToEdit.getPasswordBlob(), masterKey, entryToEdit.getIv()), StandardCharsets.UTF_8)); 
            } catch (Exception ignored) {}
        }

        JButton btnSave = new JButton("Save Vault Entry");
        btnSave.putClientProperty("JButton.buttonType", "roundRect");
        btnSave.setBackground(UIManager.getColor("Component.accentColor"));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            try {
                String rawNewPass = new String(txtPass.getPassword());
                byte[] passIv = CryptoUtil.generateIV();
                byte[] encPass = CryptoUtil.encrypt(rawNewPass.getBytes(StandardCharsets.UTF_8), masterKey, passIv);
                
                byte[] userIv = CryptoUtil.generateIV();
                byte[] encUser = CryptoUtil.encrypt(txtUser.getText().getBytes(StandardCharsets.UTF_8), masterKey, userIv);
                byte[] userPayload = new byte[userIv.length + encUser.length];
                System.arraycopy(userIv, 0, userPayload, 0, userIv.length);
                System.arraycopy(encUser, 0, userPayload, userIv.length, encUser.length);
                String b64User = java.util.Base64.getEncoder().encodeToString(userPayload);

                if (entryToEdit == null) {
                    vaultDAO.insertEntry(new VaultEntry(0, txtSite.getText(), b64User, encPass, passIv, txtNotes.getText(), attachmentData[0], new ArrayList<>(), new ArrayList<>()));
                } else {
                    entryToEdit.setSiteName(txtSite.getText());
                    entryToEdit.setUsername(b64User);
                    
                    // Only add to history if the password actually changed
                    String oldPass = "";
                    try { oldPass = new String(CryptoUtil.decrypt(entryToEdit.getPasswordBlob(), masterKey, entryToEdit.getIv()), StandardCharsets.UTF_8); } catch (Exception ignored) {}
                    if (!oldPass.equals(rawNewPass)) {
                        entryToEdit.setNewPassword(encPass, passIv);
                    }
                    
                    entryToEdit.setSecureNotes(txtNotes.getText());
                    entryToEdit.setEncryptedDocumentContent(attachmentData[0]);
                    vaultDAO.updateEntry(entryToEdit);
                }
                dialog.dispose();
                refreshTable();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dialog, "Error saving entry: " + ex.getMessage()); }
        });

        gbc.gridx = 1; gbc.gridy = 5; gbc.ipady = 12;
        dialog.add(btnSave, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void downloadAttachment() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null && entry.getEncryptedDocumentContent().length > 0) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save Attachment Manually");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    byte[] payload = entry.getEncryptedDocumentContent();
                    byte[] iv = Arrays.copyOfRange(payload, 0, 12);
                    byte[] cipher = Arrays.copyOfRange(payload, 12, payload.length);
                    byte[] raw = CryptoUtil.decrypt(cipher, masterKey, iv);
                    try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                        fos.write(raw);
                    }
                    JOptionPane.showMessageDialog(this, "Attachment saved successfully to: " + fc.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error downloading attachment: " + ex.getMessage()); }
            }
        }
    }

    private void showHistoryDialog() {
        VaultEntry entry = getSelectedEntry();
        if (entry == null || entry.getPasswordHistoryBlobs().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No password history available for this entry.");
            return;
        }

        JDialog historyDialog = new JDialog(this, "Password History for " + entry.getSiteName(), true);
        historyDialog.setLayout(new BorderLayout(10, 10));

        DefaultTableModel histModel = new DefaultTableModel(new String[]{"Version", "Password (Decrypted)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable histTable = new JTable(histModel);
        histTable.setRowHeight(35);
        
        List<byte[]> blobs = entry.getPasswordHistoryBlobs();
        List<byte[]> ivs = entry.getPasswordHistoryIvs();
        // Show newest first (optional, but usually better)
        for (int i = blobs.size() - 1; i >= 0; i--) {
            try {
                String dec = new String(CryptoUtil.decrypt(blobs.get(i), masterKey, ivs.get(i)), StandardCharsets.UTF_8);
                histModel.addRow(new Object[]{"v" + (i + 1), dec});
            } catch (Exception e) { histModel.addRow(new Object[]{"v" + (i + 1), "DECRYPTION ERROR"}); }
        }

        JButton btnRevert = new JButton("Revert Current Password to Selected Version");
        btnRevert.putClientProperty("JButton.buttonType", "roundRect");
        btnRevert.addActionListener(e -> {
            int row = histTable.getSelectedRow();
            if (row != -1) {
                // Convert back to original index since we displayed in reverse
                int originalIndex = blobs.size() - 1 - row;
                if (entry.revertToPassword(originalIndex)) {
                    try {
                        vaultDAO.updateEntry(entry);
                        refreshTable();
                        historyDialog.dispose();
                        JOptionPane.showMessageDialog(this, "Successfully reverted to v" + (originalIndex + 1));
                    } catch (SQLException ex) { JOptionPane.showMessageDialog(historyDialog, "Revert failed: " + ex.getMessage()); }
                }
            } else {
                JOptionPane.showMessageDialog(historyDialog, "Please select a version to revert to.");
            }
        });

        historyDialog.add(new JScrollPane(histTable), BorderLayout.CENTER);
        historyDialog.add(btnRevert, BorderLayout.SOUTH);
        historyDialog.setSize(500, 400);
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }

    private VaultEntry getSelectedEntry() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        int id = (int) tableModel.getValueAt(row, 0);
        return currentEntries.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    private void editSelectedEntry() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) showAddEditDialog(entry);
        else JOptionPane.showMessageDialog(this, "Please select an entry to edit.");
    }

    private void deleteSelectedEntry() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + entry.getSiteName() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    vaultDAO.deleteEntry(entry.getId());
                    refreshTable();
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage()); }
            }
        }
    }

    private void copySelectedPassword() {
        VaultEntry entry = getSelectedEntry();
        if (entry != null) {
            try {
                String decrypted = new String(CryptoUtil.decrypt(entry.getPasswordBlob(), masterKey, entry.getIv()), StandardCharsets.UTF_8);
                ClipboardManager.copyToClipboard(decrypted);
                JOptionPane.showMessageDialog(this, "Password copied to clipboard.");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Copy failed."); }
        }
    }

    private void logout() {
        Arrays.fill(masterKey, (byte) 0);
        ClipboardManager.clearClipboard();
        this.dispose();
        parentLogin.setVisible(true);
    }
}
