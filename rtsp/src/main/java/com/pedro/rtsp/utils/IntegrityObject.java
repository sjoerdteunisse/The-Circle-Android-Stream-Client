package com.pedro.rtsp.utils;

import android.util.Base64;
import com.pedro.rtsp.rtsp.RtpFrame;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

public class IntegrityObject {
    private String id;
    private int sequence;
    private String signature;

    //Todo: Encrypt Hash with Asymetric key
    //Method: Takes an RTP frame as input, and uses this to calculate the hash on the Buffer.
    //Buffer contains the raw frame data of either an ACC Audio buffer or H264 buffer.
    //To consolidate the integrity this object is synced on the server to check if-
    //a sequence object is valid by comparing the received buffer vs the original buffer hash.
    public IntegrityObject(RtpFrame rtpFrame) throws NoSuchAlgorithmException {
        this.id = Storage.fetchId();
        this.sequence = rtpFrame.getSequence();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        this.signature = android.util.Base64.encodeToString(digest.digest(rtpFrame.getBuffer()), android.util.Base64.DEFAULT);
        try{
            byte[] buffer = rtpFrame.getBuffer();
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(Storage.fetchPrivateKey());
            sig.update(buffer);
            byte[] ds = sig.sign();
//            System.out.println("--PUBLIC: "+Storage.fetchPublicKey());
//
//            sig.initVerify(Storage.fetchPublicKey());
//            sig.update(buffer);
//            boolean validSignature = sig.verify(ds);
//
//            System.out.println("--valid?:"+validSignature);

            this.signature = Base64.encodeToString(ds, Base64.DEFAULT);
        } catch (InvalidKeyException e){
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        System.out.println("--POST: "+signature);
    }
}
