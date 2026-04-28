package com.lockbox.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VaultEntry implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization

    private int id;
    private String siteName;
    private String username;
    private byte[] passwordBlob; // Current password blob
    private byte[] iv; // Current password IV

    // New fields for enhanced features
    private String secureNotes;
    private byte[] encryptedDocumentContent;
    private List<byte[]> passwordHistoryBlobs = new ArrayList<>(); // List of previous password blobs
    private List<byte[]> passwordHistoryIvs = new ArrayList<>(); // List of IVs corresponding to the password history blobs

    // Constructor for existing entries (potentially without history/notes)
    public VaultEntry(int id, String siteName, String username, byte[] passwordBlob, byte[] iv) {
        this.id = id;
        this.siteName = siteName;
        this.username = username;
        this.passwordBlob = passwordBlob;
        this.iv = iv;
        this.secureNotes = ""; // Default to empty
        this.encryptedDocumentContent = new byte[0]; // Default to empty
        this.passwordHistoryBlobs = new ArrayList<>(); // Initialize empty list
        this.passwordHistoryIvs = new ArrayList<>(); // Initialize empty list
    }

    // Constructor for new entries or when loading from DB with all fields
    public VaultEntry(int id, String siteName, String username, byte[] passwordBlob, byte[] iv, String secureNotes, byte[] encryptedDocumentContent, List<byte[]> passwordHistoryBlobs, List<byte[]> passwordHistoryIvs) {
        this.id = id;
        this.siteName = siteName;
        this.username = username;
        this.passwordBlob = passwordBlob;
        this.iv = iv;
        this.secureNotes = secureNotes != null ? secureNotes : "";
        this.encryptedDocumentContent = encryptedDocumentContent != null ? encryptedDocumentContent : new byte[0];
        this.passwordHistoryBlobs = (passwordHistoryBlobs != null) ? new ArrayList<>(passwordHistoryBlobs) : new ArrayList<>();
        this.passwordHistoryIvs = (passwordHistoryIvs != null) ? new ArrayList<>(passwordHistoryIvs) : new ArrayList<>();
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getSiteName() { return siteName; }
    public String getUsername() { return username; }
    public byte[] getPasswordBlob() { return passwordBlob; }
    public byte[] getIv() { return iv; }
    public String getSecureNotes() { return secureNotes; }
    public byte[] getEncryptedDocumentContent() { return encryptedDocumentContent; }
    public List<byte[]> getPasswordHistoryBlobs() { return Collections.unmodifiableList(passwordHistoryBlobs); }
    public List<byte[]> getPasswordHistoryIvs() { return Collections.unmodifiableList(passwordHistoryIvs); }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public void setUsername(String username) { this.username = username; }
    
    /**
     * Sets the current password and adds the previous current password to history.
     * @param newPasswordBlob The new password bytes.
     * @param newIv The IV for the new password.
     */
    public void setNewPassword(byte[] newPasswordBlob, byte[] newIv) {
        // If there's an existing password, add it to history before replacing
        if (this.passwordBlob != null && this.iv != null) {
            this.passwordHistoryBlobs.add(this.passwordBlob);
            this.passwordHistoryIvs.add(this.iv);
        }
        this.passwordBlob = newPasswordBlob;
        this.iv = newIv;
    }

    public void setSecureNotes(String secureNotes) { this.secureNotes = (secureNotes != null) ? secureNotes : ""; }
    public void setEncryptedDocumentContent(byte[] encryptedDocumentContent) { this.encryptedDocumentContent = (encryptedDocumentContent != null) ? encryptedDocumentContent : new byte[0]; }

    // --- Password History Management ---

    /**
     * Reverts to a previous password from history.
     * @param index The index of the password in history to revert to.
     * @return true if successful, false otherwise.
     */
    public boolean revertToPassword(int index) {
        if (index >= 0 && index < passwordHistoryBlobs.size()) {
            // Move current password to history
            if (this.passwordBlob != null && this.iv != null) {
                this.passwordHistoryBlobs.add(this.passwordBlob);
                this.passwordHistoryIvs.add(this.iv);
            }
            // Revert to the selected history item
            this.passwordBlob = this.passwordHistoryBlobs.get(index);
            this.iv = this.passwordHistoryIvs.get(index);

            // Remove the reverted password from history
            this.passwordHistoryBlobs.remove(index);
            this.passwordHistoryIvs.remove(index);
            return true;
        }
        return false;
    }

    // --- Serialization Helper Methods ---
    // These are for managing the List<byte[]> fields within a single BLOB column in the DB

    public static byte[] serializeList(List<? extends Serializable> list) throws IOException {
        if (list == null) {
            return null;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(list);
            return bos.toByteArray();
        }
    }

    public static List<byte[]> deserializeByteArrayList(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) {
            return new ArrayList<>();
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                List<?> byteLists = (List<?>) obj;
                List<byte[]> result = new ArrayList<>();
                for (Object item : byteLists) {
                    if (item instanceof byte[] bs) {
                        result.add(bs);
                    } else {
                        // Handle case where list contains non-byte[] elements if necessary,
                        // though ideally it shouldn't happen if serialized correctly.
                        // For now, we'll just skip or throw an error.
                        System.err.println("Warning: Encountered non-byte[] element during deserialization of byte list.");
                    }
                }
                return result;
            }
        }
        return new ArrayList<>(); // Return empty list if deserialization fails or type is unexpected
    }
}
