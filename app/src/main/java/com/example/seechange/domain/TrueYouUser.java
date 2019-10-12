package com.example.seechange.domain;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

public class TrueYouUser implements Serializable {
    private String firstName;
    private String prefix;
    private String lastName;
    private String description;
    private String email;
    private String residence;
    private String country;
    private String dateOfBirth;
    private String phone;

    public TrueYouUser(String firstName, String prefix, String lastName, String description, String email, String residence, String country, String dateOfBirth, String phone) {
        this.firstName = firstName;
        this.prefix = prefix;
        this.lastName = lastName;
        this.description = description;
        this.email = email;
        this.residence = residence;
        this.country = country;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getResidence() {
        return residence;
    }

    public String getCountry() {
        return country;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public static String getFullNameOfTrueYou(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        final String firstname = sharedPref.getString("saved_firstname", "dummy default firstname");
        final String prefix = sharedPref.getString("saved_prefix", "");
        final String lastname = sharedPref.getString("saved_lastname", "dummy default lastname");

        String fullName;

        if(prefix.equals("")) {
            fullName = firstname + " " + lastname;
        } else {
            fullName = firstname + " " + prefix + " " + lastname;
        }
        return fullName;
    }

    public static String getSatoshiOfTrueYou(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        final String satoshi = sharedPref.getString("saved_satoshi", "no satoshi");

        return satoshi;
    }
}
