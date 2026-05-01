package com.ask.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Utility for AES-256 encryption and decryption of sensitive data (bank account numbers).
 * Also provides SHA-256 hashing for token fingerprints.
 */
@Slf4j
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKeySpec;

    /**
     * Initialises AES encryption with the provided secret key.
     * Key must be exactly 32 characters (256 bits) for AES-256.
     *
     * @param secretKey the AES encryption key from properties
     */
    public EncryptionUtil(@Value("${ask.encryption.secret-key}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypts a plaintext string using AES-256.
     *
     * @param plainText the text to encrypt
     * @return Base64-encoded encrypted string
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256 encrypted string.
     *
     * @param encryptedText the Base64-encoded encrypted text
     * @return the decrypted plaintext string
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generates a SHA-256 hash of the given input.
     * Used for creating token fingerprints for session tracking.
     *
     * @param input the string to hash
     * @return hex-encoded SHA-256 hash
     */
    public static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 hashing failed", e);
        }
    }
}
