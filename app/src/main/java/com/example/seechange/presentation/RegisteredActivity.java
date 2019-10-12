package com.example.seechange.presentation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.seechange.R;
import com.example.seechange.service.SharedPreferencesHandler;

public class RegisteredActivity extends AppCompatActivity {
    private TextView txtPubKey;
    private ImageView btnRegisteredLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        txtPubKey = findViewById(R.id.txtRegisteredPubKey);
        btnRegisteredLogin = findViewById(R.id.btnRegisteredLogin);

        String publicKey = SharedPreferencesHandler.getStoredPublicKey(getApplicationContext());
        txtPubKey.setText(publicKey);

        btnRegisteredLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginWindow = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginWindow);
                finish();
            }
        });
    }
}
