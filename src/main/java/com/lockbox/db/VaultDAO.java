package com.lockbox.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaultDAO {

    public void insertEntry(VaultEntry entry) throws SQLException {
        String sql = "INSERT INTO vault(site_name, username, password_blob, iv) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.executeUpdate();
        }
    }

    public List<VaultEntry> getAllEntries() throws SQLException {
        List<VaultEntry> entries = new ArrayList<>();
        String sql = "SELECT id, site_name, username, password_blob, iv FROM vault";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                entries.add(new VaultEntry(
                        rs.getInt("id"),
                        rs.getString("site_name"),
                        rs.getString("username"),
                        rs.getBytes("password_blob"),
                        rs.getBytes("iv")
                ));
            }
        }
        return entries;
    }

    public void updateEntry(VaultEntry entry) throws SQLException {
        String sql = "UPDATE vault SET site_name = ?, username = ?, password_blob = ?, iv = ? WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getSiteName());
            pstmt.setString(2, entry.getUsername());
            pstmt.setBytes(3, entry.getPasswordBlob());
            pstmt.setBytes(4, entry.getIv());
            pstmt.setInt(5, entry.getId());
            pstmt.executeUpdate();
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
