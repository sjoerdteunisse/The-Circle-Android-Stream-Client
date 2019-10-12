package com.pedro.rtsp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.PrivateKey;
import java.security.PublicKey;

public class Storage{
    private String id;
    public static String finalPhonenumber;
    public static PrivateKey finalPrivateKey;
    public static PublicKey finalPublicKey;

    public static void setId(String phone) {
        finalPhonenumber = phone;
    }

    public static void setPrivateKey(PrivateKey key){finalPrivateKey = key;}

    public static void setPublicKey(PublicKey key){finalPublicKey = key;}

    public static String fetchId(){
       return finalPhonenumber;
    }

    public static PrivateKey fetchPrivateKey(){return finalPrivateKey;}

    public static PublicKey fetchPublicKey(){return finalPublicKey;}
}
