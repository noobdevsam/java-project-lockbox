package com.lockbox.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KeyDerivationTest {

    @Test
    public void testVerifyMasterPassword() throws Exception {
        char[] password = "MyMasterPassword".toCharArray();
        byte[] salt = KeyDerivation.generateSalt();

        byte[] hash = KeyDerivation.deriveKey(password, salt);

        assertTrue(KeyDerivation.verifyMasterPassword(password, salt, hash));

        char[] wrongPassword = "WrongPassword".toCharArray();
        assertFalse(KeyDerivation.verifyMasterPassword(wrongPassword, salt, hash));
    }
}
