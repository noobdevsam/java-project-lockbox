package com.lockbox.db;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VaultDAO {

    // --- Database Schema Modification ---
    // The SQL statements in DatabaseHelper.java have been updated to include:
    // secure_notes TEXT,
    // encrypted_document_content BLOB,
    // password_history_blobs BLOB,
    // password_history_ivs BLOB

    // --- Data Serialization/Deserialization Helpers ---
    // Using VaultEntry's static methods for serialization/deserialization of lists

    private byte[] serializeList(List<? extends Serializable> list) throws IOException {
        return VaultEntry.serializeList(list);
    }

    private List<byte[]> deserializeByteArrayList(byte[] data) throws IOException, ClassNotFoundException {
        return VaultEntry.deserializeByteArrayList(data);
    }


    // --- CRUD Operations ---

    public void insertEntry(VaultEntry entry) throws SQLException {
        String sql = "INSERT INTO vault(site_name, username, password_blob, iv, secure_notes, encrypted_document_content, original_file_name, password_history_blobs, password_history_ivs) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.setString(5, entry.getSecureNotes());
            pstmt.setBytes(6, entry.getEncryptedDocumentContent());
            pstmt.setString(7, entry.getOriginalFileName());

            // Serialize and set history lists
            pstmt.setBytes(8, serializeList(entry.getPasswordHistoryBlobs()));
            pstmt.setBytes(9, serializeList(entry.getPasswordHistoryIvs()));

            pstmt.executeUpdate();
        } catch (IOException e) {
            throw new SQLException("Error serializing password history for insertion: " + e.getMessage(), e);
        }
    }

    public List<VaultEntry> getAllEntries() throws SQLException {
        List<VaultEntry> entries = new ArrayList<>();
        String sql = "SELECT id, site_name, username, password_blob, iv, secure_notes, encrypted_document_content, original_file_name, password_history_blobs, password_history_ivs FROM vault";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    List<byte[]> historyBlobs = deserializeByteArrayList(rs.getBytes("password_history_blobs"));
                    List<byte[]> historyIvs = deserializeByteArrayList(rs.getBytes("password_history_ivs"));

                    entries.add(new VaultEntry(
                            rs.getInt("id"),
                            rs.getString("site_name"),
                            rs.getString("username"),
                            rs.getBytes("password_blob"),
                            rs.getBytes("iv"),
                            rs.getString("secure_notes"),
                            rs.getBytes("encrypted_document_content"),
                            rs.getString("original_file_name"),
                            historyBlobs,
                            historyIvs
                    ));
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error deserializing entry ID " + rs.getInt("id") + ": " + e.getMessage());
                }
            }
        }
        return entries;
    }

    public void updateEntry(VaultEntry entry) throws SQLException {
        String sql = "UPDATE vault SET site_name = ?, username = ?, password_blob = ?, iv = ?, secure_notes = ?, encrypted_document_content = ?, original_file_name = ?, password_history_blobs = ?, password_history_ivs = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.setString(5, entry.getSecureNotes());
            pstmt.setBytes(6, entry.getEncryptedDocumentContent());
            pstmt.setString(7, entry.getOriginalFileName());

            // Serialize and set history lists
            pstmt.setBytes(8, serializeList(entry.getPasswordHistoryBlobs()));
            pstmt.setBytes(9, serializeList(entry.getPasswordHistoryIvs()));

            pstmt.setInt(10, entry.getId());
            pstmt.executeUpdate();
        } catch (IOException e) {
            throw new SQLException("Error serializing password history for update: " + e.getMessage(), e);
        }
    }

    public void deleteEntry(int id) throws SQLException {
        // When deleting an entry, we should also clean up its password history.
        // This is handled by the foreign key constraint if we were using a separate history table.
        // For a single table approach, we just delete the row.
        String sql = "DELETE FROM vault WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Config methods (remain unchanged for now)
    public void setConfigValue(String key, byte[] value) throws SQLException {
        String sql = "INSERT INTO config (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value=excluded.value";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setBytes(2, value);
            pstmt.executeUpdate();
        }
    }

    public byte[] getConfigValue(String key) throws SQLException {
        String sql = "SELECT value FROM config WHERE key = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("value");
                }
            }
        }
        return null;
    }
}
