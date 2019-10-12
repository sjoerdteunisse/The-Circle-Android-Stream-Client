package com.example.seechange.service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    public static String hashWithSHA256(String stringToHash) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(stringToHash.getBytes());
        byte byteData[] = messageDigest.digest();
        return Encoder.encodeToHex(byteData);
    }
}
