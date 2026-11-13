package com.developersfab.Dhanushappbe.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class VaultEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12; // bytes
    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public VaultEncryptionService(
            @Value("${app.vault.encryption-key:0123456789abcdef0123456789abcdef}") String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        byte[] finalKey = new byte[32]; // AES-256
        System.arraycopy(keyBytes, 0, finalKey, 0, Math.min(keyBytes.length, 32));
        this.secretKey = new SecretKeySpec(finalKey, "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error during AES-GCM encryption", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null) return null;
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during AES-GCM decryption", e);
        }
    }
}

