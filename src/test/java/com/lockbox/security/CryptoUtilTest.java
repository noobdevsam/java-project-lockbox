package com.lockbox.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

public class CryptoUtilTest {

    @Test
    public void testEncryptionDecryption() throws Exception {
        byte[] key = new byte[32]; // 256-bit key
        byte[] iv = CryptoUtil.generateIV();
        String originalText = "SuperSecretPassword123!";

        byte[] ciphertext = CryptoUtil.encrypt(originalText.getBytes(StandardCharsets.UTF_8), key, iv);

        assertNotNull(ciphertext);
        assertNotEquals(originalText, new String(ciphertext, StandardCharsets.UTF_8));

        byte[] decryptedBytes = CryptoUtil.decrypt(ciphertext, key, iv);
        String decryptedText = new String(decryptedBytes, StandardCharsets.UTF_8);

        assertEquals(originalText, decryptedText);
    }

    @Test
    public void testTamperedCiphertext() throws Exception {
        byte[] key = new byte[32];
        byte[] iv = CryptoUtil.generateIV();
        String originalText = "Data";

        byte[] ciphertext = CryptoUtil.encrypt(originalText.getBytes(StandardCharsets.UTF_8), key, iv);

        // Tamper with the ciphertext (e.g. flip a bit)
        ciphertext[0] = (byte) ~ciphertext[0];

        assertThrows(Exception.class, () -> {
            CryptoUtil.decrypt(ciphertext, key, iv);
        }, "Decryption should fail due to AEADBadTagException");
    }
}
