SRS  
---

Project Title: LockBox

## ---

**1\. Introduction**

### **1.1 Purpose**

The purpose of this document is to define the functional and non-functional requirements for the **Automated Secure Password Manager**. This application is designed for users who prioritize local data sovereignty and "zero-knowledge" security architectures.

### **1.2 Scope**

LockBox is a Java-based desktop application. It provides a secure vault for credentials, using **SQLite** as the local storage engine and military-grade encryption for data at rest. It does not utilize cloud synchronization to eliminate remote attack vectors.

## **2\. Study of Existing Works**

Before designing LockBox, we analyzed industry leaders to identify security gaps and best practices:

* **KeePass (Local-Only):** The gold standard for local password management. It uses the .kdbx file format. While highly secure, its UI is often cited as dated and complex for average users.  
* **Bitwarden (Open Source):** Known for its transparent security audits. It uses PBKDF2 with 600,000+ iterations for key derivation. LockBox adopts this standard for local deployment.  
* **LastPass (Cloud-Based):** Suffered major breaches in 2022/2023. This reinforces the need for **local-only** managers where the user—not the provider—holds the encrypted vault.

**Key Findings:** The "Master Password" must never be stored. Instead, a derived key must be used for encryption, and a high-iteration hash should be used for authentication to prevent brute-force attacks on modern GPUs.

## 

## **3\. System Architecture**

LockBox follows a **Zero-Knowledge Architecture**. The application knows nothing about the user's data; it only provides the tools to encrypt and decrypt it using a key provided at runtime.

## **4\. Functional Requirements**

### **4.1 Master Authentication**

* **Registration:** On first run, the user must set a Master Password.  
* **Login:** The user must provide the Master Password to decrypt the SQLite database.  
* **Account Lockout:** After five failed attempts, the app will implement a progressive time delay to prevent automated brute-forcing.

### **4.2 Credential Management (CRUD)**

* **Create:** Store Site Name, URL, Username, and Password.  
* **Read:** Display accounts in a searchable JTable. Passwords remain masked (••••••) until "View" is toggled.  
* **Update:** Edit existing entries.  
* **Delete:** Remove entries with a confirmation prompt.

### **4.3 Automated Password Generator**

* Generates cryptographically strong passwords using java.security.SecureRandom.  
* Customizable parameters: length, uppercase, numbers, and special symbols.

### **4.4 Secure Clipboard**

* Copy password to clipboard for 30 seconds.  
* **Auto-Clear:** A background thread must overwrite the clipboard after the timer expires.

## **5\. Security Methodology (The "Hardest" Methods)**

To ensure maximum safety, LockBox implements the following cryptographic standards:

### **5.1 Key Derivation: PBKDF2 with HMAC-SHA256**

The Master Password is not used directly. Instead, we use **PBKDF2** (Password-Based Key Derivation Function 2):

* **Salt:** A unique 16-byte random salt stored in the DB.  
* **Iterations:** 600,000 (minimum).  
* **Derived Key:** 256 bits.  
* *Purpose:* This makes "offline" brute-force attacks extremely slow and expensive for hackers.

### **5.2 Encryption: AES-256-GCM**

While many apps use AES-CBC, LockBox uses **AES-GCM (Galois/Counter Mode)**.

* **Why GCM?** Unlike CBC, GCM provides **Authenticated Encryption**. It ensures that the data has not been tampered with by adding an Authentication Tag to the encrypted block.  
* **IV (Initialization Vector):** A unique 12-byte IV is generated for every single password entry to prevent pattern recognition.

## **6\. Non-Functional Requirements**

* **Security:** Databases must be unreadable if opened in a standard SQLite browser.  
* **Performance:** Decryption of the vault should take less than 1.5 seconds despite high PBKDF2 iterations.  
* **Usability:** A modern "Dark Mode" interface using the **FlatLaf** library.

## **7\. Database Design (SQLite)**

The SQLite file will contain a table vault:

* id: INTEGER PRIMARY KEY  
* site\_name: TEXT (Plaintext)  
* username: TEXT (Encrypted)  
* password\_blob: BLOB (The AES-GCM Ciphertext)  
* iv: BLOB (The unique 12-byte IV for this entry)

## **8\. References**

1. **NIST Special Publication 800-63B:** *Digital Identity Guidelines* regarding password complexity and storage. \[Original Source: nist.gov\]  
2. **OWASP Password Storage Cheat Sheet:** Recommendations for PBKDF2 iterations and salt lengths. \[Original Source: owasp.org\]  
3. **SQLite Documentation:** For local storage implementation. \[Original Source: sqlite.org\]  
4. **Java Cryptography Architecture (JCA) Reference Guide:** Implementation of Cipher and SecretKeyFactory. \[Original Source: oracle.com\]

*Note: This document is a proposal of a software.*