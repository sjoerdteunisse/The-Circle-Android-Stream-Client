package com.example.seechange.presentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.seechange.R;
import com.example.seechange.domain.TrueYouUser;
import com.example.seechange.service.Authenticator;
import com.example.seechange.service.Config;
import com.example.seechange.service.SharedPreferencesHandler;
import com.example.seechange.service.VolleyRequestQueue;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileInformationActivity extends AppCompatActivity {
    private Button btnEditAvatar;
    private TextView txtName;
    private TextView txtDesc;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtRes;
    private TextView txtCountry;
    private TextView txtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        txtName = findViewById(R.id.txtProfDetName);
        txtDesc = findViewById(R.id.txtProfDetDesc);
        txtEmail = findViewById(R.id.txtProfDetEmail);
        txtPhone = findViewById(R.id.txtProfDetPhone);
        txtRes = findViewById(R.id.txtProfDetRes);
        txtCountry = findViewById(R.id.txtProfDetCou);
        txtDate = findViewById(R.id.txtProfDetDate);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        final String description = sharedPref.getString("saved_description", "-- No description --");
        final String email = sharedPref.getString("saved_email", "");
        final String residence = sharedPref.getString("saved_residence", "");
        final String country = sharedPref.getString("saved_country", "");
        final String date = sharedPref.getString("saved_date", "");

        txtName.setText(TrueYouUser.getFullNameOfTrueYou(getApplicationContext()));
        txtDesc.setText(description);
        txtEmail.setText(email);
        txtPhone.setText(SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()));
        txtRes.setText(residence);
        txtCountry.setText(country);
        txtDate.setText(date);

        btnEditAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAvatar();
            }
        });

        getCurrentAvatar();
    }

    public void editAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            final Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                    long lengthbmp = byteArray.length;

                    float sizeOfImage = ((float) Math.round((lengthbmp / (1024)) * 10) / 10);

                    if(sizeOfImage < 300) {

                        final String encodedString = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                        String phoneNumber = SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext());
                        String publicKey = SharedPreferencesHandler.getStoredPublicKey(getApplicationContext());

                        Authenticator authenticator = new Authenticator(getApplicationContext());
                        // Create digital signature
                        String digiSigAvatar = authenticator.registerDigiSig(encodedString);

                        String jsonBody = "{\"avatarAsBase64\":\"" + encodedString
                                + "\",\"phoneNumber\":\"" + phoneNumber
                                + "\",\"pubKey\":\"" + publicKey
                                + "\",\"digiSigAvatar\":\"" + digiSigAvatar + "\"}";

                        JSONObject jb = new JSONObject(jsonBody);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                Config.URL_NEWAVATAR,
                                jb,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        try {
                                            // Set the image in ImageView
                                            ImageView imgAvatar = findViewById(R.id.imgProfileAvatar);
                                            imgAvatar.setImageURI(selectedImageUri);
                                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putString("saved_avatar", encodedString);
                                            editor.commit();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplicationContext(), "Something went wrong. Avatar has not been edited", Toast.LENGTH_LONG).show();
                                        try {
                                            String responseBody = new String(error.networkResponse.data, "utf-8");
                                            System.out.println(responseBody);
                                        } catch (Exception ex) {}
                                    }
                                }) {

                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };
                        VolleyRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                    } else {
                        Toast.makeText(this, "The chosen image is too large", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    public void getCurrentAvatar() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        final String base64Avatar = sharedPref.getString("saved_avatar", "");

        byte[] decodedString = Base64.decode(base64Avatar, Base64.NO_WRAP);
        InputStream inputStream  = new ByteArrayInputStream(decodedString);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);

        ImageView imgAvatar = findViewById(R.id.imgProfileAvatar);
        imgAvatar.setImageBitmap(bitmap);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
