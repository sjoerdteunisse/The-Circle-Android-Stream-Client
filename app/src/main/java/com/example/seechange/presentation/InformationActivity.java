package com.example.seechange.presentation;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.seechange.R;
import com.example.seechange.service.SharedPreferencesHandler;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

public class InformationActivity extends AppCompatActivity {
    private Button btnCopyPrivkey;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        btnCopyPrivkey = findViewById(R.id.btnCopyPriv);
        mImageView = (ImageView)findViewById(R.id.QrCodeView);

        String s =  SharedPreferencesHandler.getStoredPhoneNumber(getApplicationContext())+"@VX"+SharedPreferencesHandler.getStoredPrivateKey(getApplicationContext());

        mImageView.setImageBitmap(QRCode.from(s).to(ImageType.JPG).withSize(450,450).bitmap());

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);


        btnCopyPrivkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);

                // Creates a new text clip to put on the clipboard
                String privateKeyString = SharedPreferencesHandler.getStoredPrivateKey(getApplicationContext());
                ClipData clip = ClipData.newPlainText("priv key", privateKeyString);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(getApplicationContext(), "Copied private key to clipboard!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
