# [Goal Description]
Develop "LockBox", an Automated Secure Password Manager using a Zero-Knowledge Architecture. It utilizes Java Swing (with FlatLaf) for the UI, AES-256-GCM for encryption, PBKDF2 with HMAC-SHA256 for key derivation, and SQLite for local storage.

## User Review Required
> [!IMPORTANT]
> - **Build System:** The proposal doesn't explicitly mention a build system. I am proposing using **Maven** (`pom.xml`) to easily manage dependencies like SQLite JDBC and FlatLaf.
> - **UI Framework:** The architecture specifically mentions Java Swing with FlatLaf, which I will use for the implementation as it aligns well with the "Dark Mode" requirement.
> Let me know if you approve this approach or prefer Gradle or another alternative.

## Proposed Changes

### Project Setup
#### [NEW] [pom.xml](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/pom.xml)
Maven configuration file to include dependencies for SQLite JDBC, FlatLaf, and JUnit (for testing).
#### [NEW] [Main.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/Main.java)
Entry point of the application. Bootstraps the database and launches the `LoginFrame`.

### Security Module
#### [NEW] [CryptoUtil.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/security/CryptoUtil.java)
Handles AES-256-GCM encryption and decryption.
#### [NEW] [KeyDerivation.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/security/KeyDerivation.java)
Handles PBKDF2 with HMAC-SHA256 key derivation (600,000 iterations), salt generation, and verification.

### Database Module
#### [NEW] [DatabaseHelper.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/db/DatabaseHelper.java)
Manages the SQLite connection and initializes the `vault` and potentially a `config` table (to store the Salt and Master Password Hash).
#### [NEW] [VaultDAO.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/db/VaultDAO.java)
Data Access Object handling CRUD operations for vault entries.
#### [NEW] [VaultEntry.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/db/VaultEntry.java)
Model class for a vault entry (`id`, `site_name`, `username` (encrypted), `password_blob`, `iv`).

### Presentation Layer (UI)
#### [NEW] [ClipboardManager.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/ui/ClipboardManager.java)
Utility to handle copying passwords to the clipboard and auto-clearing after 30 seconds using a background thread timer.
#### [NEW] [LoginFrame.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/ui/LoginFrame.java)
Registration and Login screen. Handles Master Password input, derives main key, and implements a progressive time delay lockout on 5 failed attempts.
#### [NEW] [DashboardFrame.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/ui/DashboardFrame.java)
Main vault display (`JTable`) with searchable interface, masking passwords (••••••), and full CRUD actions.
#### [NEW] [PasswordGeneratorDialog.java](file:///home/sam/MyProjects/JavaProjects/java-project-lockbox/src/main/java/com/lockbox/ui/PasswordGeneratorDialog.java)
UI element to generate cryptographically strong passwords using `java.security.SecureRandom`.

## Verification Plan

### Automated Tests
- Unit tests for `CryptoUtil` ensuring that data encrypted can be decrypted only with the correct key and that tampering with ciphertext triggers an authentication failure.
- Unit tests for `KeyDerivation` to verify PBKDF2 implementation and speed sanity checks.
- Unit tests for `VaultDAO` using an in-memory SQLite database to test all CRUD functions.

### Manual Verification
- Run the application and set up the master password on the first run.
- Close and reopen, then test both valid and invalid logins.
- Test the 5-attempt lockout mechanism.
- Add, View, Edit, and Delete several mock credentials.
- Test password generated against custom rules (length, numbers, symbols).
- Send a password to the clipboard and wait 30 seconds to ensure it is cleared.
- Verify through an external SQLite viewer that the `vault` database is securely encrypted by AES-GCM and displays only meaningless BLOBs/ciphertext.
