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

    // --- Data Serialization/Deserialization Helpers ---
    private byte[] serializeList(List<? extends Serializable> list) throws IOException {
        return VaultEntry.serializeList(list);
    }

    private List<byte[]> deserializeByteArrayList(byte[] data) throws IOException, ClassNotFoundException {
        return VaultEntry.deserializeByteArrayList(data);
    }

    // --- CRUD Operations ---

    public void insertEntry(VaultEntry entry) throws SQLException {
        String sql = "INSERT INTO vault(site_name, username, password_blob, iv, secure_notes, secure_notes_iv, encrypted_document_content, original_file_name, original_file_name_iv, password_history_blobs, password_history_ivs) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.setBytes(5, entry.getSecureNotes());
            pstmt.setBytes(6, entry.getSecureNotesIv());
            pstmt.setBytes(7, entry.getEncryptedDocumentContent());
            pstmt.setBytes(8, entry.getOriginalFileName());
            pstmt.setBytes(9, entry.getOriginalFileNameIv());

            // Serialize and set history lists
            pstmt.setBytes(10, serializeList(entry.getPasswordHistoryBlobs()));
            pstmt.setBytes(11, serializeList(entry.getPasswordHistoryIvs()));

            pstmt.executeUpdate();
        } catch (IOException e) {
            throw new SQLException("Error serializing password history for insertion: " + e.getMessage(), e);
        }
    }

    public List<VaultEntry> getAllEntries() throws SQLException {
        List<VaultEntry> entries = new ArrayList<>();
        String sql = "SELECT id, site_name, username, password_blob, iv, secure_notes, secure_notes_iv, encrypted_document_content, original_file_name, original_file_name_iv, password_history_blobs, password_history_ivs FROM vault";
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
                            rs.getBytes("secure_notes"),
                            rs.getBytes("secure_notes_iv"),
                            rs.getBytes("encrypted_document_content"),
                            rs.getBytes("original_file_name"),
                            rs.getBytes("original_file_name_iv"),
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
        String sql = "UPDATE vault SET site_name = ?, username = ?, password_blob = ?, iv = ?, secure_notes = ?, secure_notes_iv = ?, encrypted_document_content = ?, original_file_name = ?, original_file_name_iv = ?, password_history_blobs = ?, password_history_ivs = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.setBytes(5, entry.getSecureNotes());
            pstmt.setBytes(6, entry.getSecureNotesIv());
            pstmt.setBytes(7, entry.getEncryptedDocumentContent());
            pstmt.setBytes(8, entry.getOriginalFileName());
            pstmt.setBytes(9, entry.getOriginalFileNameIv());

            // Serialize and set history lists
            pstmt.setBytes(10, serializeList(entry.getPasswordHistoryBlobs()));
            pstmt.setBytes(11, serializeList(entry.getPasswordHistoryIvs()));

            pstmt.setInt(12, entry.getId());
            pstmt.executeUpdate();
        } catch (IOException e) {
            throw new SQLException("Error serializing password history for update: " + e.getMessage(), e);
        }
    }

    public void deleteEntry(int id) throws SQLException {
        String sql = "DELETE FROM vault WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // Config methods
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
