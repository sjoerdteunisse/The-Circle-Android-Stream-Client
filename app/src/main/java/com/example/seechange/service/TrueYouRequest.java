package com.example.seechange.service;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.seechange.domain.TrueYouUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

public class TrueYouRequest {
    private Context context;
    private TrueYouRequest.TrueYouListener listener;

    public TrueYouRequest(Context context, TrueYouRequest.TrueYouListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void handlePostTrueYouUser(final TrueYouUser newTrueYouUser, HashMap<Integer, String> hashMap) {
        // Make an JSON object
        String bodyOfRequest =
                "{\"firstname\":\"" + newTrueYouUser.getFirstName() +
                "\",\"prefix\":\"" + newTrueYouUser.getPrefix() +
                "\",\"lastname\":\"" + newTrueYouUser.getLastName() +
                "\",\"description\":\"" + newTrueYouUser.getDescription() +
                "\",\"email\":\"" + newTrueYouUser.getEmail() +
                "\",\"residence\":\"" + newTrueYouUser.getResidence() +
                "\",\"country\":\"" + newTrueYouUser.getCountry() +
                "\",\"dateOfBirth\":\"" + newTrueYouUser.getDateOfBirth() +
                "\",\"phone\":\"" + newTrueYouUser.getPhone() +
                "\",\"publicKey\":\"" + hashMap.get(1) +
                "\",\"digiSig\":\"" + hashMap.get(2) + "\"}";

        try {
            JSONObject jsonBody = new JSONObject(bodyOfRequest);

            // Post request to the nodejs server endpoint: /api/user/register
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    Config.URL_REGISTER,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject resultObject =  response.getJSONObject("result");
                                String firstname = resultObject.getString("firstname");
                                String lastname = resultObject.getString("lastname");
                                String email = resultObject.getString("email");
                                String digSignature = resultObject.getString("digSignature");

                                String raw = firstname + lastname + email;

                                // Create signature
                                Signature signature1 = Signature.getInstance("SHA256withRSA");

                                // Decode base64 digSignature
                                final byte[] bytesDigSig = Base64.decode(digSignature.getBytes(), Base64.DEFAULT);

                                // Remove head and footer of the public key of the server (from the config file)
                                PublicKey publicKeyWithoutHeads = KeyHandler.getPublicKey(Config.PUBLIC_KEY_SERVER);

                                signature1.initVerify(publicKeyWithoutHeads);
                                signature1.update(raw.getBytes());

                                //Verifying the signature
                                boolean verified = signature1.verify(bytesDigSig);
                                if(verified) {
                                    // If there is a response callback method
                                    listener.onTrueYouUserAvailable(newTrueYouUser);
                                } else {
                                    Toast.makeText(context, "Your connection is not private!", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError || error instanceof NoConnectionError){
                                Toast.makeText(context, "Could not connect to the Circle server", Toast.LENGTH_LONG).show();
                            }
                            else{
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    listener.onTrueYouUserError(responseBody);
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
            VolleyRequestQueue.getInstance(context).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException ex) {
            listener.onTrueYouUserError(ex.getMessage());
        }
    }

    public interface TrueYouListener {
        // Callback function to handle a added trueyouuser.
        void onTrueYouUserAvailable(TrueYouUser trueYouUser);

        // Callback to handle serverside API errors
        void onTrueYouUserError(String message);
    }
}
