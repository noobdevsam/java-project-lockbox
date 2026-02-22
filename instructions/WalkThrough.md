# LockBox Walkthrough

LockBox has been fully implemented based on the Zero-Knowledge Architecture requirements. Below is a summary of the accomplishments and how to run the application.

## 1. Application Architecture Implemented
- **Security Logic:** Added AES-256-GCM authenticated encryption through [CryptoUtil.java](java-project-lockbox/src/main/java/com/lockbox/security/CryptoUtil.java) and PBKDF2 Key Derivation using HMAC-SHA256 (600,000 iterations) through [KeyDerivation.java](java-project-lockbox/src/main/java/com/lockbox/security/KeyDerivation.java).
- **Database Logic:** Created an SQLite initialization engine mapped through [DatabaseHelper.java](java-project-lockbox/src/main/java/com/lockbox/db/DatabaseHelper.java) and a robust [VaultDAO.java](java-project-lockbox/src/main/java/com/lockbox/db/VaultDAO.java) supporting complete CRUD (Create, Read, Update, Delete) capability.
- **Presentation Logic (UI):** Connected everything into a Java Swing application using FlatLaf Dark theme. Includes Login setup, 5-strike lockout, a dynamic Vault Dashboard, secure clipboard clearing, and an automated Password Generator dialog.
- **Build Module:** Provided a [pom.xml](java-project-lockbox/pom.xml) loaded with JUnit, FlatLaf, SQlite-JDBC, and the `maven-shade-plugin` to assemble everything into a standalone executable.

## 2. Testing Details
Automated JUnit tests have been provided inside `src/test/java/com/lockbox/security/`:
1. `CryptoUtilTest.java`: Verifies proper encapsulation and explicit failure modes if ciphertexts are tampered (Authentication tags).
2. `KeyDerivationTest.java`: Verifies PBKDF2 speed, match validity, and uniqueness.
These were successfully triggered via `mvn test`.

## 3. Manual Verification Steps
To verify and run this application yourself, execute the following from the root directory:

```bash
# Clean, compile, test, and build the runnable JAR
mvn clean package

# Run the locked application
java -jar target/lockbox-1.0-SNAPSHOT.jar
```

### Try these actions:
1. **First Start Setup:** Input a master password on the very first screen to generate your salted Hash and establish your local SQLite vault at `~/.lockbox/vault.db`.
2. **Dashboard Testing:** Add a new entry (Site: *GitHub*, Password: *generate one*). Try copying it to the clipboard; it will safely automatically clear after precisely 30 seconds.
3. **Lockout Testing:** Quit the application and restart it. Intentionally fail the login 5 times. You will be locked out with an exponential progressive delay.
4. **Security Check:** Inspect the `~/.lockbox/vault.db` file using a standard SQLite browser. Notice that the vault only displays unrecognizable Blobs (ciphertext) for passwords and initialized vectors, while maintaining your Zero-Knowledge safety.