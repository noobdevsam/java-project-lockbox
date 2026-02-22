package com.lockbox.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class KeyDerivation {
    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static byte[] deriveKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] encoded = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return encoded;
    }

    public static boolean verifyMasterPassword(char[] inputPassword, byte[] storedSalt, byte[] storedHash) {
        try {
            byte[] testHash = deriveKey(inputPassword, storedSalt);
            boolean match = Arrays.equals(testHash, storedHash);
            Arrays.fill(testHash, (byte) 0);
            return match;
        } catch (Exception e) {
            return false;
        }
    }
}
