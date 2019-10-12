package com.example.seechange.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

public class SharedPreferencesHandler {
    public static String getStoredPrivateKey(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        return sharedPreferences.getString("privateKey", null);
    }
    public static String getStoredPublicKey(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        return sharedPreferences.getString("publicKey", null);
    }
    public static String getStoredPhoneNumber(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        return sharedPreferences.getString("phoneNumber", null);
    }
    public static Boolean checkIfCreated(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        boolean active = sharedPreferences.getBoolean("account_created", false);
        if (active){
            return true;
        }else{
            return false;
        }
    }
    public static void wipe(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("privateKey");
        editor.remove("saved_prefix");
        editor.remove("phoneNumber");
        editor.remove("saved_firstname");
        editor.remove("saved_avatar");
        editor.remove("saved_satoshi");
        editor.remove("account_created");
        editor.remove("publicKey");
        editor.remove("saved_email");
        editor.remove("saved_username");
        editor.remove("saved_lastname");
        editor.apply();
    }
    public static void printSharedPreferences(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        Map<String,?> keys = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " + entry.getValue().toString());
        }
    }
    public static boolean insertIntoSharedPreferences(Context context, String prefKey, String objectToInsert ){
        SharedPreferences sharedPref = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefKey, objectToInsert);
        editor.apply();
        if (!(sharedPref.getString(prefKey, objectToInsert).isEmpty())){
            return true;
        }else{
            throw new Error("Could not save the specified string to the SharedPreferences of this device.");
        }
    }
}
