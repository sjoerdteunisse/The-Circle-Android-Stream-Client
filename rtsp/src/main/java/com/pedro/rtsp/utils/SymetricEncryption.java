package com.pedro.rtsp.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.security.Key;
import javax.crypto.Cipher;
import java.security.MessageDigest;
import javax.crypto.spec.SecretKeySpec;

public class SymetricEncryption {

    private static final String privateKey = new String("MIIEogIBAAKCAQEA45KofNF2WzpSx8FWiDCqjHGAQL6pffz6VXxMNPbUNi4LuX85SgmdW0lAsmt7cYtkL1qoF4YGL661fkEQLaxPnyfImOGoQtPlrFZaITNc7fAF8kOakrLFVp0Z+1ZGQHW/e+I5ekz/8vLe4jm/sG1+eR8zn7YcRhxP1CcHLMEn8a03KKki/aSyZ4smtbIMEEswnwkPXnKWNMx0mFnKaamKqxxV0wrXREqDbGHTiNPiLY5cLbRNIK/XUKr2J+krVvSsZ2N7Iqe0itDAHf0++R2XBmCdk8KvbTXCKhNlLY/ZxY/r8+aulIfJArVloEa2OGICUUTvUtVK0+XfKkvYOwNd+wIDAQABAoIBAEvj2XoeY+jST068A75Q9F73bryomF8iYNznIYa8FVKoElLewV0WTbpmeEPHohfgUx2TjuChWijGi1G6IsoIX1kE7FlHT1RR05KgDVhrB391fpYmw0JOiY5zv3lYcP8IBT4r190YFnaPVZJ9jZYpPXbo189X7J/YyypFhZgAJfDqoKJNRWLYXDlxNjjFPEwhffFB68Z4VjWuDmBv7heODF2wQEesKDF2TKndUQo1YdMIBVnZbjmqJtXGpSQI9NRHSO0gR8+nRmItWYg+lwTsrG5JQFswuO9SIK9h2a0928QaF8hz8OpvogV/tGSSej/tQfzXcYY1UVml2ikQ4lo3UVECgYEA+AhvoPaYhxlAEa6cJ2HunxNY/phJ2PnNzo57r7RmOgXWZZrcYNlqYW0mifds/Ymf2mqT/6V/r37AoPoNu2x604/Tj13aDnBAhJyWCXCk8ZrUOlHYvahW1NdK7Us4gJwIgzEzumw9aofPaj3TA33sWPffw1+PGHbrSZzojs+Z4zMCgYEA6uH62A/z69gTKl64mAvxmXGLb9M37HiX34LgedoHuqIOc5mHH8bVPNqyfFoS5Q6k4fSeWfp8mqkfg2eUm1jaAFXa/nPijp/d7pNVz7Z2W50HvFt+z4eerZV5Tqy70MIZjI0B3RzFBzZjkHu7OSkEnWlzD0eRHzNAtC8p06xzGhkCgYBLKptRx4mf35oYAbeXA8nB8aYVuecfV0z0nR+EtwIH8X7w2caRnzHrBp4YT6B7EXsdQRFK90fD0oDkCAOjWhO7C6pMl1+e4ova5G1fvQBvq8FHT0iE6YAvDnPJh/M+norB32ZSx7yxDSlhhAzv0PiQWcY5OjtuaZOSgnfD18xGGwKBgDaZJPgIbUjLR1k3/2zIifaDIEh0LrG3LoZ0I73vYpCNjxJsA67tRzAIHiktvuqjxswHUq/TvBcvbPXco/ygYLxnOKqhQW6p1qrYcMABx+1vyRaNpiKTgd0Tu8j/+gCCvfGf7Wj6gQo2KFqziEe3My1sD6Tjm+ZGwamgtPZRRcgxAoGAH2z08IXtlR4pMn81PFg+o6Z/eUDM/JdN/o/6Re/dJZ3vcU82oWpeMzjVm/khHeQ+OPW9b8EON2FjA+WZOMGroTtK8Iak5Ig0g0ITAEH9u4bMyX7RiZpjBHWW6qa4ep0O45965AYY5UVV0DxWHl/wC2fHxABz6H82/4sNYfyTZDA=");

    private static final String publicKey = new String("MIIBCgKCAQEA45KofNF2WzpSx8FWiDCqjHGAQL6pffz6VXxMNPbUNi4LuX85SgmdW0lAsmt7cYtkL1qoF4YGL661fkEQLaxPnyfImOGoQtPlrFZaITNc7fAF8kOakrLFVp0Z+1ZGQHW/e+I5ekz/8vLe4jm/sG1+eR8zn7YcRhxP1CcHLMEn8a03KKki/aSyZ4smtbIMEEswnwkPXnKWNMx0mFnKaamKqxxV0wrXREqDbGHTiNPiLY5cLbRNIK/XUKr2J+krVvSsZ2N7Iqe0itDAHf0++R2XBmCdk8KvbTXCKhNlLY/ZxY/r8+aulIfJArVloEa2OGICUUTvUtVK0+XfKkvYOwNd+wIDAQAB");


    @TargetApi(Build.VERSION_CODES.O)
    public static String encrypt(final byte[] frameBytes)  {
        byte[] encryptedValue = null;
        try {
            Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encrypt.init(Cipher.ENCRYPT_MODE, loadPrivateKey(privateKey));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                encryptedValue = encrypt.doFinal(frameBytes);
            }
        }catch (Exception e){
            System.out.println("error="+ e);
        }


        return android.util.Base64.encodeToString(encryptedValue, android.util.Base64.DEFAULT);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static PrivateKey loadPrivateKey(String privateKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("");
        } catch (InvalidKeySpecException e) {
            throw new Exception("?");
        } catch (NullPointerException e) {
            throw new Exception("?");
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static PublicKey loadPublicKey(String publicKey)
            throws Exception {
        try {//from ww w  .  j a v  a2s  .c  o m
            byte[] buffer = Base64.getDecoder().decode(publicKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("");
        } catch (InvalidKeySpecException e) {
            throw new Exception("?");
        } catch (NullPointerException e) {
            throw new Exception("?");
        }
    }

    public static String decrypt(final String encryptedValue) {

        String decryptedValue = null;

        try {

            Cipher decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            decrypt.init(Cipher.DECRYPT_MODE, loadPrivateKey(publicKey));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                byte[] encryptedMessage = decrypt.doFinal(encryptedValue.getBytes(StandardCharsets.UTF_8));
                decryptedValue = new String(encryptedMessage);
            }
        }catch (Exception e){
            System.out.println("error="+ e);
        }


        return decryptedValue;
    }
}
