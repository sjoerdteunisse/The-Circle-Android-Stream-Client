package com.example.seechange.presentation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.seechange.R;
import com.example.seechange.service.AuthRequest;
import com.example.seechange.service.Authenticator;
import com.example.seechange.service.SharedPreferencesHandler;

public class LoginActivity extends AppCompatActivity  implements AuthRequest.AuthListener {
    private TextView txtLoginError;
    private TextView txtRegisterLabel;
    private ImageView btnRegister;
    private SharedPreferences sharedPreferences;
    private Context context;

    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_login);

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

        // UNCOMMENT TO COMPLETELY WIPE ALL SHAREDPREFERENCES (NOT REVERSIBLE)
        //  SharedPreferencesHandler.wipe(context);
        // Print all SharedPreferences for debug purposes
        // SharedPreferencesHandler.printSharedPreferences(context);

        txtLoginError = (TextView) findViewById(R.id.txtLoginError);
        btnRegister = (ImageView) findViewById(R.id.btnLoginRegister);
        txtRegisterLabel = (TextView) findViewById(R.id.txtLoginLabel);

        Boolean accountAlreadyCreated = SharedPreferencesHandler.checkIfCreated(context);
        if(accountAlreadyCreated) {
            btnRegister.setImageResource(R.drawable.ic_person_black_24dp);
            txtRegisterLabel.setText("Your key");
        }

        // Send an authentication request if an account was made before
        String phoneNumber = SharedPreferencesHandler.getStoredPhoneNumber(context);
        Boolean active = SharedPreferencesHandler.checkIfCreated(context);
        if (phoneNumber != null && active){
            // Hide register icon
//            btnRegister.setVisibility(View.GONE);
//            txtRegisterLabel.setVisibility(View.GONE);
            Authenticator authenticator = new Authenticator(context);
            // Init digital signature
            String digiSig = authenticator.initSignature();
            if (digiSig != null){
                // Send authentication request
                AuthRequest authRequest = new AuthRequest(context, this);
                authRequest.handleAuth(phoneNumber, digiSig);
            }
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean accountAlreadyCreated = SharedPreferencesHandler.checkIfCreated(context);
                if(!accountAlreadyCreated) {
                    Intent registerWindow = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(registerWindow);
                } else {
                    Intent registeredWindow = new Intent(getApplicationContext(), RegisteredActivity.class);
                    startActivity(registeredWindow);
                }
            }
        });
    }

    @Override
    public void onAuthAvailable() {
        Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_LONG).show();
        Intent loggedIn = new Intent(context, MainActivity.class);
        startActivity(loggedIn);
        finish();
    }
    @Override
    public void onAuthError(String message) {
        try {
            if(message.contains("not active")) {
                txtLoginError.setText("Please go to The Circle to activate your account");
            } else if(message.contains("Phone number of")) {
                txtLoginError.setText("Your registered phone number does not exist in our system");
            } else if(message.contains("integrity")) {
                txtLoginError.setText("Your connection is not private");
            }
            else {
                txtLoginError.setText("Something went wrong..");
            }
        } catch (Exception ex) {
            txtLoginError.setText("Something went wrong..");
        }

    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
