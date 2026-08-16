package com.blazingmc.protocol.handler;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class EncryptionUtil {
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_MODE = "AES/CFB8/NoPadding";
    private static final int RSA_KEY_SIZE = 1024;
    
    private final KeyPair rsaKeyPair;
    
    public EncryptionUtil() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(RSA_KEY_SIZE);
        this.rsaKeyPair = keyPairGenerator.generateKeyPair();
    }
    
    public PublicKey getPublicKey() {
        return rsaKeyPair.getPublic();
    }
    
    public PrivateKey getPrivateKey() {
        return rsaKeyPair.getPrivate();
    }
    
    public byte[] getEncodedPublicKey() {
        return rsaKeyPair.getPublic().getEncoded();
    }
    
    public byte[] decryptSharedSecret(byte[] encryptedSharedSecret) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        return cipher.doFinal(encryptedSharedSecret);
    }
    
    public byte[] decryptVerifyToken(byte[] encryptedVerifyToken) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        return cipher.doFinal(encryptedVerifyToken);
    }
    
    public static byte[] generateVerifyToken() {
        byte[] verifyToken = new byte[4];
        new SecureRandom().nextBytes(verifyToken);
        return verifyToken;
    }
    
    public static byte[] generateSharedSecret() {
        byte[] sharedSecret = new byte[16];
        new SecureRandom().nextBytes(sharedSecret);
        return sharedSecret;
    }
    
    public static SecretKeySpec createAESKey(byte[] sharedSecret) {
        return new SecretKeySpec(sharedSecret, AES_ALGORITHM);
    }
    
    public static Cipher createAESEncryptCipher(byte[] sharedSecret) throws Exception {
        SecretKeySpec keySpec = createAESKey(sharedSecret);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(sharedSecret));
        return cipher;
    }
    
    public static Cipher createAESDecryptCipher(byte[] sharedSecret) throws Exception {
        SecretKeySpec keySpec = createAESKey(sharedSecret);
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(sharedSecret));
        return cipher;
    }
    
    public static String generateServerHash(String serverId, byte[] sharedSecret, byte[] publicKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(serverId.getBytes());
            digest.update(sharedSecret);
            digest.update(publicKey);
            
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}