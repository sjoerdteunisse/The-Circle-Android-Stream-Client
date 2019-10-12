package com.example.seechange.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.seechange.R;
import com.example.seechange.domain.TrueYouUser;
import com.example.seechange.service.Authenticator;
import com.example.seechange.service.Config;
import com.example.seechange.service.KeyHandler;
import com.example.seechange.service.OnSwipeTouchListener;
import com.example.seechange.service.SharedPreferencesHandler;
import com.example.seechange.service.VolleyRequestQueue;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.pedro.encoder.input.gl.SpriteGestureController;
import com.pedro.encoder.input.gl.render.filters.*;
import com.pedro.encoder.input.gl.render.filters.object.GifObjectFilterRender;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.encoder.utils.gl.TranslateTo;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtsp.rtsp.Protocol;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;
import com.pedro.rtsp.utils.Storage;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static com.example.seechange.service.Config.URL_GETSATOSHI;
import static com.example.seechange.service.Config.URL_STARTSTREAM;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, ConnectCheckerRtsp, SurfaceHolder.Callback, View.OnTouchListener {
    private RtspCamera1 rtspCamera1;
    private ImageView btnRec, btnSwitchCam, btnPause;
    private OpenGlView openGlView;
    private TextView txtMenuName;
    private ImageView btnMenuRefresh;
    private TextView txtMenuSatoshi;
    private ImageView imgAvatar;
    private int swipeCount;
    private boolean paused;
    private Socket socket;
    private int viewCount = 0;
    private TextView txtViewCounter;
    private SpriteGestureController spriteGestureController = new SpriteGestureController();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Keep window alive when user is on this activity.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Used for integrity
        final SharedPreferences sharedPreferences = context.getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        Storage.setId(sharedPreferences.getString("phoneNumber", null));
        try{
            Storage.setId(SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()));
            String privateKey = SharedPreferencesHandler.getStoredPrivateKey(getApplicationContext());
//            String publicKey = SharedPreferencesHandler.getStoredPublicKey(getApplicationContext());
            Storage.setPrivateKey(KeyHandler.getPrivateKey(privateKey));
//            Storage.setPublicKey(KeyHandler.getPublicKey(publicKey));
        }catch(GeneralSecurityException e){
            e.printStackTrace();
        }



        openGlView = findViewById(R.id.surfaceView);
        openGlView.getHolder().addCallback(this);
        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(context);

        openGlView.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeUp() {
                setFilter(true);
            }

            @Override
            public void onSwipeDown() {
                setFilter(false);
            }
        });
        rtspCamera1 = new RtspCamera1(openGlView, this);
        rtspCamera1.setProtocol(Protocol.TCP);
        rtspCamera1.setReTries(10);

        btnRec = findViewById(R.id.btnStreamRec);
        btnSwitchCam = findViewById(R.id.btnStreamSwitchCam);
        btnPause = findViewById(R.id.btnPauseRec);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseStream();
            }
        });
        txtViewCounter = findViewById(R.id.txtViewerCount);
        paused = false;
        btnSwitchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        // Navigation
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txtMenuName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtMenuName);
        txtMenuSatoshi = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txtMenuSatoshi);
        btnMenuRefresh = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.btnMenuRefresh);
        imgAvatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imgAvatar);

        btnMenuRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSatoshi(sharedPreferences.getString("phoneNumber", null));
            }
        });

        txtMenuName.setText(TrueYouUser.getFullNameOfTrueYou(getApplicationContext()));
        // Get satoshi balance of database
        getSatoshi(sharedPreferences.getString("phoneNumber", null));

        getAvatar();
    }
    private Emitter.Listener onNewCount = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject j = (JSONObject) args[0];
                    try {
                        // Remove head and footer of the public key of the server (from the config file)
                        PublicKey publicKeyWithoutHeads = KeyHandler.getPublicKey(Config.PUBLIC_KEY_SERVER);
                        String count = j.getString("count");
                        String digiSig = j.getString("digisig");
                        Signature sig = Signature.getInstance("SHA256withRSA");
                        // Decode base64 digSignature
                        final byte[] bytesDigSig = Base64.decode(digiSig.getBytes(), Base64.DEFAULT);
                        // Verify
                        sig.initVerify(publicKeyWithoutHeads);
                        sig.update(count.getBytes());
                        //V erifying the signature
                        boolean verified = sig.verify(bytesDigSig);
                        int intViewers = Integer.parseInt(count);
                        if (!(intViewers < 0)){
                            System.out.println("verified viewers:"+ count);
                            txtViewCounter.setText("" + count);
                        }
                        else{
                            System.out.println("unverified");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };


    public void startRecording() {
        if (!rtspCamera1.isStreaming()) {
            if (rtspCamera1.isRecording()
                    || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                // Notify the user by updating button
                btnRec.setBackgroundResource(R.drawable.rounded_button);
                btnRec.setImageResource(R.drawable.ic_videocam_white_24dp);

                rtspCamera1.startStream(URL_STARTSTREAM + SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()));
                System.out.println(URL_STARTSTREAM + SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()));

                try {
                    txtViewCounter.setVisibility(View.VISIBLE);
                    viewCount = 0;
                    txtViewCounter.setText("" + viewCount);
                    socket = IO.socket(Config.URL_GETVIEWCOUNT);
                    socket.connect();
                    socket.on("viewCount" + SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()), onNewCount);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            } else {
                txtViewCounter.setVisibility(View.VISIBLE);
                viewCount = 0;
                txtViewCounter.setText("" + viewCount);
                socket.disconnect();
                socket.off("viewCount" + SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext()));
                Toast.makeText(this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Notify the user is offline by updating the button
            txtViewCounter.setVisibility(View.INVISIBLE);
            rtspCamera1.stopStream();
            btnRec.setBackgroundResource(R.drawable.rounded_button_offline);
            btnRec.setImageResource(R.drawable.ic_videocam_black_24dp);

        }
    }

    public void switchCamera() {
        try {
            rtspCamera1.switchCamera();
        } catch (CameraOpenException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseStream(){
//        paused = paused ? false : true;
        paused = !paused;
        btnPause.setImageResource(paused ? R.drawable.ic_visibility_off_white_24dp : R.drawable.ic_visibility_white_24dp);
        System.out.println(paused);
        if (paused) {
            System.out.println("--STREAM pausing");
            rtspCamera1.disableAudio();
        }else{
            System.out.println("--STREAM continue");
            rtspCamera1.enableAudio();
        }
        rtspCamera1.getGlInterface().setFilter(paused ? new BlackFilterRender() : new NoFilterRender());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        rtspCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (rtspCamera1.isStreaming()) {
            rtspCamera1.stopStream();
            btnRec.setBackgroundResource(R.drawable.rounded_button_offline);
            btnRec.setImageResource(R.drawable.ic_videocam_black_24dp);
        }
        rtspCamera1.stopPreview();
    }

    @Override
    public void onConnectionSuccessRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onConnectionFailedRtsp(final String reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                        .show();
                rtspCamera1.stopStream();
                btnRec.setBackgroundResource(R.drawable.rounded_button_offline);
                btnRec.setImageResource(R.drawable.ic_videocam_black_24dp);
            }
        });
    }

    @Override
    public void onDisconnectRtsp() {

    }

    @Override
    public void onAuthErrorRtsp() {

    }

    @Override
    public void onAuthSuccessRtsp() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void getAvatar() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        String base64Avatar = sharedPref.getString("saved_avatar", "");
        byte[] decodedString = Base64.decode(base64Avatar, Base64.NO_WRAP);
        InputStream inputStream = new ByteArrayInputStream(decodedString);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        imgAvatar.setImageBitmap(bitmap);
    }

    public void getSatoshi(String phoneNr) {
        String url = URL_GETSATOSHI + phoneNr;
        JsonObjectRequest satoshiRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
            try {

                JSONObject resultObject = response.getJSONObject("result");

                // Get values from request
                String satoshiBalance = resultObject.getString("satoshiBalance");
                String digSignature = resultObject.getString("digSignature");

                // Create signature
                Signature signature1 = Signature.getInstance("SHA256withRSA");

                // Decode base64 digSignature
                final byte[] bytesDigSig = Base64.decode(digSignature.getBytes(), Base64.DEFAULT);

                // Remove head and footer of the public key of the server (from the config file)
                PublicKey publicKeyWithoutHeads = KeyHandler.getPublicKey(Config.PUBLIC_KEY_SERVER);

                signature1.initVerify(publicKeyWithoutHeads);
                signature1.update(satoshiBalance.getBytes());

                //Verifying the signature
                boolean verified = signature1.verify(bytesDigSig);
                if(verified) {
                    txtMenuSatoshi.setText("Satoshi balance: " + satoshiBalance);
                } else {
                    Toast.makeText(getApplicationContext(), "Your connection is not private!", Toast.LENGTH_LONG).show();
                }

            } catch (Exception ex) {
                System.out.println(ex);
            }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        VolleyRequestQueue.getInstance(getApplicationContext()).addToRequestQueue(satoshiRequest);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_close) {
            finishAffinity();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_login_inf_website) {
            Intent informationWindow = new Intent(getApplicationContext(), InformationActivity.class);
            startActivity(informationWindow);
        } else if (id == R.id.nav_profile_details) {
            Intent profileWindow = new Intent(getApplicationContext(), ProfileInformationActivity.class);
            startActivity(profileWindow);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setFilter(boolean increase) {
        spriteGestureController.setBaseObjectFilterRender(null);
        int max = 20;
        int min = 0;
        if (paused) { return; }
        if (increase){ swipeCount++; }
        else{ swipeCount--; }
        if (swipeCount < min){ swipeCount = max; }
        else if (swipeCount > max){ swipeCount = min; }
        System.out.println(swipeCount);
        switch (swipeCount) {
            case 0:
                // No filtering
                rtspCamera1.getGlInterface().setFilter(new NoFilterRender());
                break;
            case 1:
                // Lamoish(?) filter
                rtspCamera1.getGlInterface().setFilter(new LamoishFilterRender());
                break;
            case 2:
                // Beauty filter
                rtspCamera1.getGlInterface().setFilter(new BeautyFilterRender());
                break;
            case 3:
                // Temperature filter
                rtspCamera1.getGlInterface().setFilter(new TemperatureFilterRender());
                break;
            case 4:
                // RGB Saturation filter
                RGBSaturationFilterRender rgbSaturationFilterRender = new RGBSaturationFilterRender();
                rtspCamera1.getGlInterface().setFilter(rgbSaturationFilterRender);
                //Reduce green and blue colors 20%. Red will predominate.
                rgbSaturationFilterRender.setRGBSaturation(1f, 0.8f, 0.8f);
                break;
            case 5:
                // Brightness filter
                rtspCamera1.getGlInterface().setFilter(new BrightnessFilterRender());
                break;
            case 6:
                // Negative filter
                rtspCamera1.getGlInterface().setFilter(new NegativeFilterRender());
                break;
            case 7:
                // Sharpness filter
                rtspCamera1.getGlInterface().setFilter(new SharpnessFilterRender());
                break;
            case 8:
                // Color filter
                rtspCamera1.getGlInterface().setFilter(new ColorFilterRender());
                break;
            case 9:
                // Contrast filter
                rtspCamera1.getGlInterface().setFilter(new ContrastFilterRender());
                break;
            case 10:
                // Duotone filter (Blue/green)
                rtspCamera1.getGlInterface().setFilter(new DuotoneFilterRender());
                break;
            case 11:
                // Early bird filter
                rtspCamera1.getGlInterface().setFilter(new EarlyBirdFilterRender());
                break;
            case 12:
                // Detection filter
                rtspCamera1.getGlInterface().setFilter(new EdgeDetectionFilterRender());
                break;
            case 13:
                // Exposure filter
                rtspCamera1.getGlInterface().setFilter(new ExposureFilterRender());
                break;
            case 14:
                // Fire filter
                rtspCamera1.getGlInterface().setFilter(new FireFilterRender());
                break;
            case 15:
                // Gamma filter
                rtspCamera1.getGlInterface().setFilter(new GammaFilterRender());
                break;
            case 16:
                // Saturation filter
                rtspCamera1.getGlInterface().setFilter(new SaturationFilterRender());
                break;
            case 17:
                // GreyScale filter
                rtspCamera1.getGlInterface().setFilter(new GreyScaleFilterRender());
                break;
            case 18:
                // Sepia filter
                rtspCamera1.getGlInterface().setFilter(new SepiaFilterRender());
                break;
            case 19:
                // 70's filter
                rtspCamera1.getGlInterface().setFilter(new Image70sFilterRender());
                break;
            case 20:
                rtspCamera1.getGlInterface().setFilter(new NoFilterRender());
                setGifToStream();
                default:
                    break;
        }
    }

    private void setGifToStream() {
        try {
            GifObjectFilterRender gifObjectFilterRender = new GifObjectFilterRender();
            rtspCamera1.getGlInterface().setFilter(gifObjectFilterRender);
            gifObjectFilterRender.setGif(getResources().openRawResource(R.raw.banana));
            gifObjectFilterRender.setDefaultScale(rtspCamera1.getStreamWidth(),
                    rtspCamera1.getStreamHeight());
            gifObjectFilterRender.setPosition(TranslateTo.BOTTOM);
            spriteGestureController.setBaseObjectFilterRender(gifObjectFilterRender); //Optional
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (spriteGestureController.spriteTouched(view, motionEvent)) {
            spriteGestureController.moveSprite(view, motionEvent);
            spriteGestureController.scaleSprite(motionEvent);
            return true;
        }
        return false;
    }
}

