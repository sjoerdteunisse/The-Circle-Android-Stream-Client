package com.pedro.rtsp.utils;

import android.util.Base64;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TransferObject {

    private String trasnferObject;
    private String hashOfObject;

    public TransferObject(Object transferObject) throws Exception {
        Gson gson = new Gson();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        if(transferObject == null)
            throw new Exception();

        this.trasnferObject = android.util.Base64.encodeToString(gson.toJson(transferObject).getBytes(), Base64.DEFAULT);
        this.hashOfObject = android.util.Base64.encodeToString(digest.digest(this.trasnferObject.getBytes()), Base64.DEFAULT);
    }
}
