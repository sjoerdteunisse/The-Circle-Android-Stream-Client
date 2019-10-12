package com.pedro.rtsp.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;

import com.pedro.rtsp.rtsp.RtpFrame;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IntegrityObjectCopy {

    private int sequence;
    private String signature;

    @TargetApi(Build.VERSION_CODES.O)
    public IntegrityObjectCopy(RtpFrame rtpFrame) throws NoSuchAlgorithmException {
         this.sequence = rtpFrame.getSequence();


         //Hash Val
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(rtpFrame.getBuffer());
        //byte[] fullyEncoded = digest.digest(rtpFrame.)
        //returns a base64 encrypted objcryt
         this.signature = Base64.encodeToString(encodedhash, Base64.DEFAULT);//SymetricEncryption.encrypt(encodedhash);
        //System.out.println(SymetricEncryption.decrypt(Base64.encodeToString(java.util.Base64.getDecoder().decode(signature),Base64.NO_WRAP) + " @--COMPARED TO--@ " + encodedhash));



        System.out.println("sequence frame " + sequence + " with sig " + signature);
    }
}
