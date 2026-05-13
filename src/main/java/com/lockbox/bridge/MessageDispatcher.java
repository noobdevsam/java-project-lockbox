package com.lockbox.bridge;

import com.lockbox.db.VaultDAO;
import com.lockbox.db.VaultEntry;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;

/**
 * Dispatches messages from the browser extension to the internal LockBox services.
 */
public class MessageDispatcher {
    
    private final VaultDAO vaultDAO;

    public MessageDispatcher(VaultDAO vaultDAO) {
        this.vaultDAO = vaultDAO;
    }

    /**
     * Handles the request JSON and returns a response JSON string.
     * Note: A production system should use a real JSON library like Jackson or Gson.
     */
    public String handle(String jsonRequest) {
        if (!AuthManager.getInstance().isUnlocked()) {
            return "{\"status\":\"error\", \"message\":\"Vault is locked. Please unlock the desktop application first.\"}";
        }
        
        try {
            if (jsonRequest.contains("\"action\":\"GET_ENTRY\"")) {
                // Simplified extraction: assumes {"action":"GET_ENTRY", "site":"example.com"}
                String site = extractValue(jsonRequest, "site");
                return getEntryJson(site);
            }
        } catch (Exception e) {
            return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
        }
        
        return "{\"status\":\"error\", \"message\":\"Unknown action\"}";
    }

    private String getEntryJson(String site) throws SQLException {
        List<VaultEntry> entries = vaultDAO.getAllEntries();
        for (VaultEntry entry : entries) {
            if (entry.getSiteName().equalsIgnoreCase(site)) {
                String passBase64 = Base64.getEncoder().encodeToString(entry.getPasswordBlob());
                return String.format("{\"status\":\"success\", \"username\":\"%s\", \"password_blob\":\"%s\"}", 
                                     entry.getUsername(), passBase64);
            }
        }
        return "{\"status\":\"error\", \"message\":\"Entry not found\"}";
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
