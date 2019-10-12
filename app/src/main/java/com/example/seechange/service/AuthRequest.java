package com.example.seechange.service;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

public class AuthRequest {
    private Context context;
    private AuthRequest.AuthListener listener;

    public AuthRequest(Context context, AuthRequest.AuthListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void handleAuth(final String phoneNumber, String digitalSignature) {
        String body = "{\"phoneNumber\":\"" + phoneNumber + "\",\"digitalSignature\":\"" + digitalSignature + "\"}";
        try {
            JSONObject jsonBody = new JSONObject(body);
            JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_AUTH, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject resultObject = response.getJSONObject("result");
                        String firstName = resultObject.getString("firstname");
                        String prefix = resultObject.getString("prefix");
                        String lastName = resultObject.getString("lastname");
                        String email = resultObject.getString("email");
                        String satoshiValue = resultObject.getString("satoshi");
                        String avatarUrl = resultObject.getString("avatarUrl");
                        String description = resultObject.getString("description");
                        String residence = resultObject.getString("residence");
                        String country = resultObject.getString("country");
                        String date = resultObject.getString("dateOfBirth");

                        // Get digital signature from request
                        String digitalSignature = resultObject.getString("digiSig");
                        // Rebuild string used for verifying
                        String raw = firstName + lastName + description + email + residence + country;
                        // Create signature
                        Signature sig = Signature.getInstance("SHA256withRSA");
                        // Decode base64 digSignature
                        final byte[] bytesDigSig = Base64.decode(digitalSignature.getBytes(), Base64.DEFAULT);
                        // Remove head and footer of the public key of the server (from the config file)
                        PublicKey publicKeyWithoutHeads = KeyHandler.getPublicKey(Config.PUBLIC_KEY_SERVER);
                        // Verify
                        sig.initVerify(publicKeyWithoutHeads);
                        sig.update(raw.getBytes());

                        //Verifying the signature
                        boolean verified = sig.verify(bytesDigSig);
                        if(!verified) {
                            Toast.makeText(context, "Your connection is not private!", Toast.LENGTH_LONG).show();
                        }

                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_email", email);
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_firstname", firstName);
                        if (!prefix.equals("")) {
                            SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_prefix", prefix);
                        }
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_lastname", lastName);
                        if (!description.equals("")) {
                            SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_description", description);
                        }
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_residence", residence);
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_country", country);
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_date", date);
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_satoshi", satoshiValue);
                        SharedPreferencesHandler.insertIntoSharedPreferences(context, "saved_avatar", avatarUrl);

                        // If there is a response callback method
                        listener.onAuthAvailable();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(context, "Could not connect to the Circle server", Toast.LENGTH_LONG).show();
                    }
                    else{
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            listener.onAuthError(responseBody);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }) {
                public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
                }
            };
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                    1500, // SOCKET_TIMEOUT_MS,
                    2, // DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Access the RequestQueue through your singleton class.
            VolleyRequestQueue.getInstance(context).addToRequestQueue(jsObjRequest);
        } catch (JSONException ex) {
            listener.onAuthError(ex.getMessage());
        }
        return;
    }

    public interface AuthListener {
        // Callback function to handle a added authentication response.
        void onAuthAvailable();

        // Callback to handle serverside API errors
        void onAuthError(String message);
    }
}
