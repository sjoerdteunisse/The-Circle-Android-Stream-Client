package com.example.seechange;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import com.example.seechange.domain.TrueYouUser;
import com.example.seechange.service.Hasher;
import com.example.seechange.service.KeyHandler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.KeyGenerator;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GeneralTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.example.seechange", appContext.getPackageName());
    }
    @Test
    public void generateValidKeyPair(){
        try {
        // Arrange
        KeyHandler keyGenerator = KeyHandler.getInstance();
        // Act
        String privBegin = ("-----BEGIN PRIVATE KEY-----\n");
        String privCore = Base64.encodeToString(keyGenerator.getPrivateKey().getEncoded(), Base64.DEFAULT);
        String privEnd = ("-----END PRIVATE KEY-----");
        String privateKey = privBegin + privCore + privEnd;
        String pubBegin = ("-----BEGIN PUBLIC KEY-----\n");
        String pubCore = Base64.encodeToString(keyGenerator.getPublicKey().getEncoded(), Base64.DEFAULT);
        String pubEnd = ("-----END PUBLIC KEY-----");
        String publicKey = pubBegin + pubCore + pubEnd;
        PrivateKey privObj = KeyHandler.getPrivateKey(privateKey);
        PublicKey pubObj = KeyHandler.getPublicKey(publicKey);
        String phoneNumber = "0681174267";
        byte[] phoneBytes = phoneNumber.getBytes();
        Signature sig;
        sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privObj);
        sig.update(phoneBytes);
        byte[] digitalSignature = sig.sign();
        sig.initVerify(pubObj);
        sig.update(phoneBytes);
        boolean validSignature = sig.verify(digitalSignature);
        if (validSignature){
            // Assert
            Assert.assertTrue(validSignature);
        }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void createTrueYouUserObject(){
        // Arrange
        boolean created;
        TrueYouUser newTrueYouUser = new TrueYouUser("TestFirstName", "TextPrefix", "TestLastName", "TestDescription", "TestEmail@Testmail.com", "TestResidence", "TestCountry", "1998-15-01", "0681174266");
        // Act
        created = newTrueYouUser.getFirstName() != null || newTrueYouUser.getPrefix() != null || newTrueYouUser.getLastName() != null || newTrueYouUser.getDescription() != null || newTrueYouUser.getEmail() != null || newTrueYouUser.getResidence() != null || newTrueYouUser.getCountry() != null || newTrueYouUser.getDateOfBirth() != null || newTrueYouUser.getPhone() != null;
        // Assert
        Assert.assertTrue(created);
    }
    @Test
    public void hashSHA256(){
        // Arrange
        boolean validHash;
        String stringToBeHashed = "Test";
        try {
            // Act
            String hash = Hasher.hashWithSHA256(stringToBeHashed);
            if (hash.length() != 0){
                validHash = true;
            }else{
                validHash = false;
            }
            // Assert
            Assert.assertTrue(validHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
