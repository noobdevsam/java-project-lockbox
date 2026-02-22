package com.lockbox.db;

public class VaultEntry {
    private int id;
    private String siteName;
    private String username;
    private byte[] passwordBlob;
    private byte[] iv;

    public VaultEntry(int id, String siteName, String username, byte[] passwordBlob, byte[] iv) {
        this.id = id;
        this.siteName = siteName;
        this.username = username;
        this.passwordBlob = passwordBlob;
        this.iv = iv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public byte[] getPasswordBlob() { return passwordBlob; }
    public void setPasswordBlob(byte[] passwordBlob) { this.passwordBlob = passwordBlob; }
    public byte[] getIv() { return iv; }
    public void setIv(byte[] iv) { this.iv = iv; }
}
