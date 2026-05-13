package com.lockbox.bridge;

import java.util.Arrays;

/**
 * Singleton manager to track the application's unlocked state and hold the master key securely.
 */
public class AuthManager {
    private static AuthManager instance;
    private byte[] masterKey;
    private boolean isUnlocked = false;

    private AuthManager() {}

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public synchronized void setMasterKey(byte[] key) {
        if (this.masterKey != null) {
            Arrays.fill(this.masterKey, (byte) 0);
        }
        this.masterKey = key != null ? key.clone() : null;
        this.isUnlocked = (key != null);
    }

    public synchronized boolean isUnlocked() {
        return isUnlocked;
    }

    public synchronized byte[] getMasterKey() {
        return masterKey;
    }

    public synchronized void logout() {
        if (masterKey != null) {
            Arrays.fill(masterKey, (byte) 0);
        }
        masterKey = null;
        isUnlocked = false;
    }
}
