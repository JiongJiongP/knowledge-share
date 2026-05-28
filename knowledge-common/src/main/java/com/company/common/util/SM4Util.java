package com.company.common.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class SM4Util {

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS7Padding";
    private static final int IV_LENGTH = 16;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /** Generate a random 128-bit SM4 key, returned as hex string */
    public static String generateKeyHex() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(128, new SecureRandom());
        SecretKey key = kg.generateKey();
        return bytesToHex(key.getEncoded());
    }

    /** Generate a random 128-bit SM4 key, returned as base64 string */
    public static String generateKeyBase64() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(128, new SecureRandom());
        SecretKey key = kg.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /** Encrypt plaintext with hex-encoded key. Returns base64(ciphertext). */
    public static String encrypt(String plaintext, String keyHex) throws Exception {
        byte[] keyBytes = hexToBytes(keyHex);
        SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Prepend IV to ciphertext
        byte[] combined = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /** Decrypt base64(ciphertext) with hex-encoded key. Returns plaintext. */
    public static String decrypt(String encrypted, String keyHex) throws Exception {
        byte[] keyBytes = hexToBytes(keyHex);
        SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

        byte[] combined = Base64.getDecoder().decode(encrypted);

        // Extract IV from first 16 bytes
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] ciphertext = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(ciphertext);

        return new String(decrypted, "UTF-8");
    }

    /** Deterministic encryption: IV = SHA-256(plaintext)[0:16]. Same plaintext → same ciphertext. */
    public static String encryptDeterministic(String plaintext, String keyHex) throws Exception {
        byte[] keyBytes = hexToBytes(keyHex);
        SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM);

        byte[] iv = deriveIV(plaintext);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

        byte[] combined = new byte[IV_LENGTH + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
        System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /** Decrypt deterministic ciphertext. Same as decrypt(). */
    public static String decryptDeterministic(String encrypted, String keyHex) throws Exception {
        return decrypt(encrypted, keyHex);
    }

    /** Derive 16-byte IV from plaintext using SHA-256 */
    private static byte[] deriveIV(String plaintext) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(plaintext.getBytes("UTF-8"));
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(hash, 0, iv, 0, IV_LENGTH);
        return iv;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /** CLI tool: generate key or encrypt a value */
    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "genkey".equals(args[0])) {
            String keyHex = generateKeyHex();
            System.out.println("SM4 Key (hex): " + keyHex);
            System.out.println("Set SM4_KEY=" + keyHex + " in environment");
        } else if ("encrypt".equals(args[0]) && args.length == 3) {
            String keyHex = args[1];
            String plaintext = args[2];
            String encrypted = encrypt(plaintext, keyHex);
            System.out.println("SM4(" + encrypted + ")");
        } else {
            System.out.println("Usage:");
            System.out.println("  java SM4Util genkey");
            System.out.println("  java SM4Util encrypt <keyHex> <plaintext>");
        }
    }
}
