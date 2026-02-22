**Project Proposal**

Project Name: LockBox

This proposal outlines the development of **LockBox**, a high-integrity, local-first credential management solution. In an era of increasing cloud-provider breaches, LockBox returns control to the user by combining industry-standard Java security APIs with a hardened local SQLite database.

## ---

**1\. Introduction**

As digital footprints expand, the average user manages dozens of unique credentials. This has led to two dangerous behaviors: "password recycling" or reliance on centralized cloud-based managers. While convenient, cloud managers represent a single point of failure and an attractive target for sophisticated state-level and independent hackers. **LockBox** is a desktop-based "Zero-Knowledge" utility designed to eliminate these risks by ensuring that sensitive data never leaves the user's local hardware.

## **2\. Problem Statement**

The current password management landscape suffers from three primary issues:

* **Vulnerability of Centralization:** Cloud-based managers (e.g., LastPass) are susceptible to server-side breaches, exposing millions of vaults simultaneously.  
* **Insecure Local Storage:** Many simple password managers store data in plaintext or use weak encryption (like Base64 or simple XOR ciphers) that can be bypassed in seconds.  
* **Browser Dependency:** Browser-integrated managers often store credentials in a way that other local applications or malware can easily extract.

## **3\. Objectives**

The primary objectives of this project are:

* **Hardened Encryption:** To implement a "double-wall" security layer using **PBKDF2** for key derivation and **AES-256-GCM** for data encryption.  
* **Data Sovereignty:** To utilize **SQLite** as a localized, serverless database engine, ensuring the user has total physical control over their vault file.  
* **Zero-Knowledge Architecture:** To ensure that the application itself never knows the Master Password; it only facilitates the math required to unlock the data.  
* **Modern UI/UX:** To provide a streamlined, "Flat Design" interface that makes security accessible rather than cumbersome.

## **4\. Scope of the Project**

The project will cover the following development areas:

* **Database Module:** Designing an encrypted SQLite schema to store binary large objects (BLOBs) for initialization vectors (IVs) and ciphertexts.  
* **Security Module:** Implementing the javax.crypto package to handle high-iteration hashing and authenticated encryption.  
* **GUI Module:** A Java Swing or JavaFX interface featuring a secure dashboard, a dynamic password generator, and a clipboard management system.  
* **Exclusions:** This project will **not** include cloud syncing, mobile cross-platform support, or browser extensions, focusing strictly on the security of the local desktop environment.

## **5\. Proposed System Overview**

The system operates on a logic flow that separates authentication from encryption.

1. **Key Derivation:** When the user enters a Master Password ($P$), the system retrieves a unique Salt ($S$) from the database. It then applies the PBKDF2 algorithm:  
   $$DK \= \\text{PBKDF2}(HMAC\\text{-}SHA256, P, S, 600000, 256)$$  
   The resulting Derived Key ($DK$) is used as the AES-256 key.  
2. **Authenticated Encryption:** For every entry, a unique 12-byte Initialization Vector ($IV$) is generated. The password is then encrypted using AES-GCM, which produces the ciphertext and an authentication tag to prevent tampering.  
3. **Local Storage:** The $IV$, Salt, and Ciphertext are stored in an SQLite file. Without the Master Password to recreate the $DK$, the database remains a collection of meaningless bytes.

## **6\. Expected Outcomes**

Upon completion, the project will deliver:

* **A Standalone Executable:** A JAR or native installer for the LockBox application.  
* **Encrypted Local Vault:** A .db file that is cryptographically secure even if stolen.  
* **Security Documentation:** A guide explaining the implementation of the AES-GCM and PBKDF2 parameters.  
* **Feature-Complete UI:** A functional dashboard allowing for full CRUD (Create, Read, Update, Delete) operations on user credentials with automated "Clip-and-Clear" clipboard safety.

