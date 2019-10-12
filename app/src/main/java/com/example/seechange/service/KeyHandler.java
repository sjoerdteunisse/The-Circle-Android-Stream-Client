package com.example.seechange.service;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyHandler {
    private KeyPairGenerator keyPairGenerator;
    private KeyPair keyPair;
    private static KeyHandler instance;

    private KeyHandler() {
        try {
            this.keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            this.keyPairGenerator.initialize(1024, new SecureRandom());
            this.keyPair = generate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private KeyPair generate() {
        return keyPairGenerator.generateKeyPair();
    }

    public PrivateKey getPrivateKey() {
        return this.keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return this.keyPair.getPublic();
    }

    public static KeyHandler getInstance() {
        if (instance == null) {
            instance = new KeyHandler();
        }
        return instance;
    }
    public static PrivateKey getPrivateKey(String key) throws GeneralSecurityException {
        // Remove the first and last lines
        String privateKeyString = key.replace("-----BEGIN PRIVATE KEY-----", "");
        privateKeyString = privateKeyString.replace("-----END PRIVATE KEY-----", "");
        // Base64 decode the data
        byte[] encoded = android.util.Base64.decode(privateKeyString, android.util.Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        PrivateKey privateKey = kf.generatePrivate(keySpec);
        return privateKey;
    }
    public static PublicKey getPublicKey(String key) throws GeneralSecurityException {
        // Remove the first and last lines
        String publicKeyString = key.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyString = publicKeyString.replace("-----END PUBLIC KEY-----", "");
        // Base64 decode the data
        byte[] encoded = android.util.Base64.decode(publicKeyString, android.util.Base64.DEFAULT);
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);
        return publicKey;
    }
}