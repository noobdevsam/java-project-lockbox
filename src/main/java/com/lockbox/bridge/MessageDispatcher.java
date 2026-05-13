package com.lockbox.bridge;

import com.lockbox.db.VaultDAO;
import com.lockbox.security.CryptoUtil;

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
     */
    public String handle(String jsonRequest) {
        // Basic JSON parsing (to be replaced with actual library like Jackson or Gson)
        // For now, implementing a simple routing mechanism
        if (jsonRequest.contains("\"action\":\"GET_PASSWORD\"")) {
            return "{\"status\":\"success\", \"password\":\"mocked-password\"}";
        }
        
        return "{\"status\":\"error\", \"message\":\"Unknown action\"}";
    }
}
