package com.example.seechange.presentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.seechange.R;
import com.example.seechange.domain.TrueYouUser;
import com.example.seechange.service.AuthRequest;
import com.example.seechange.service.Authenticator;
import com.example.seechange.service.Encoder;
import com.example.seechange.service.KeyHandler;
import com.example.seechange.service.Hasher;
import com.example.seechange.service.SharedPreferencesHandler;
import com.example.seechange.service.TrueYouRequest;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.regex.Pattern;
public class RegisterActivity extends AppCompatActivity implements TrueYouRequest.TrueYouListener {
    private ImageView btnRegisterLogin;
    private Button btnRegister;
    private EditText firstname;
    private EditText prefix;
    private EditText lastname;
    private EditText description;
    private EditText email;
    private EditText residence;
    private EditText country;
    private EditText dateofbirth;
    private EditText phone;
    private HashMap<Integer, String> hashKeyList;

    private String blockCharacterSet = "+~<>'\"";

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source != null && blockCharacterSet.contains(("" + source))) {
                return "";
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegisterLogin = findViewById(R.id.btnRegisterLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegisterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginWindow = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginWindow);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                registerAccount();
                  }
        });

        firstname = findViewById(R.id.txtRegisterFirstn);
        firstname.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(50) });
        prefix = findViewById(R.id.txtRegisterPrefix);
        prefix.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(10)  });
        lastname = findViewById(R.id.txtRegisterLastn);
        lastname.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(100)  });
        description = findViewById(R.id.txtRegisterDesc);
        description.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(500)  });
        email = findViewById(R.id.txtRegisterEmail);
        email.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(255)  });
        residence = findViewById(R.id.txtRegisterResi);
        residence.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(50)  });
        country = findViewById(R.id.txtRegisterCountry);
        country.setFilters(new InputFilter[] { filter, new InputFilter.LengthFilter(50)  });
        dateofbirth = findViewById(R.id.txtRegisterDateBirth);
        phone = findViewById(R.id.txtRegisterPhone);
        phone.setFilters(new InputFilter[] { filter });
    }

    public void registerAccount() {
        // Get all values of the inputfields
        String txtfn = firstname.getText().toString();
        String txtpref = prefix.getText().toString();
        String txtln = lastname.getText().toString();
        String txtdes = description.getText().toString();
        String txtemail = email.getText().toString();
        String txtres = residence.getText().toString();
        String txtcountry = country.getText().toString();
        String txtdate = dateofbirth.getText().toString();
        String txtphone = phone.getText().toString();

        // Create a new TrueYouUser with these values
        TrueYouUser newTrueYouUser = new TrueYouUser(txtfn, txtpref, txtln, txtdes, txtemail, txtres, txtcountry, txtdate, txtphone);

        boolean canWeInsert = true;
        String errorMessage = "";

        // Check if there is any required field that has not filled in
        if(txtfn.equals("") || txtln.equals("") || txtemail.equals("") || txtres.equals("") || txtcountry.equals("") || txtdate.equals("") || txtphone.equals("")) {
            canWeInsert = false;
            errorMessage = "Please fill in all required fields";
        }

        // Check if filled in email is valid
        if(!txtemail.equals("") && !validEmail(txtemail)) {
            canWeInsert = false;
            errorMessage = "Email is not valid. ";
        }
        // Check if filled in date of birth is valid
        if(!txtdate.equals("") && !validDateOfBirth(txtdate)) {
            canWeInsert = false;
            errorMessage += "Date of birth is not valid";
        }

        // Check if filled in fields contains special characters like +;=#|<>^*%
        if(fieldContainsSpecCharacters(txtfn) || fieldContainsSpecCharacters(txtpref) || fieldContainsSpecCharacters(txtln) || fieldContainsSpecCharacters(txtdes) || fieldContainsSpecCharacters(txtemail) || fieldContainsSpecCharacters(txtres) || fieldContainsSpecCharacters(txtcountry) || fieldContainsSpecCharacters(txtdate) || fieldContainsSpecCharacters(txtphone)) {
            canWeInsert = false;
            errorMessage += "Please remove specials characters like +;=#|<>^*%";
        }

        if(canWeInsert) {
            registerNewAccount(newTrueYouUser);
        } else {
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    public boolean fieldContainsSpecCharacters(String text) {
        Pattern regex = Pattern.compile("[+;=#|<>^*%]");

        if (regex.matcher(text).find()) {
            return true;
        } else {
            return false;
        }
    }

    public void registerNewAccount(TrueYouUser trueYouUser) {
        // Create new request and add the user and the hashed values to it
        TrueYouRequest tyr = new TrueYouRequest(getApplicationContext(), this);
        tyr.handlePostTrueYouUser(trueYouUser, initReg(trueYouUser));
    }

    public HashMap<Integer, String> initReg (TrueYouUser trueYouUser)  {
        /// Create ArrayList in which we'll store stuff that we will POST to the server
        hashKeyList = new HashMap<>();
        // Generate keys
        KeyHandler keyGenerator = KeyHandler.getInstance();
        String privBegin = ("-----BEGIN PRIVATE KEY-----\n");
        String privCore = Base64.encodeToString(keyGenerator.getPrivateKey().getEncoded(), Base64.DEFAULT);
        String privEnd = ("-----END PRIVATE KEY-----");
        String privateKey = privBegin + privCore + privEnd;

        String pubBegin = ("-----BEGIN PUBLIC KEY-----\n");
        String pubCore = Base64.encodeToString(keyGenerator.getPublicKey().getEncoded(), Base64.DEFAULT);
        String pubEnd = ("-----END PUBLIC KEY-----");
        String publicKey = pubBegin + pubCore + pubEnd;

        // Save Keys and PhoneNumber to SharedPreferences
        Context context = getApplicationContext();
        SharedPreferencesHandler.insertIntoSharedPreferences(context, "privateKey", privateKey);
        SharedPreferencesHandler.insertIntoSharedPreferences(context, "publicKey", publicKey);
        SharedPreferencesHandler.insertIntoSharedPreferences(context, "phoneNumber", trueYouUser.getPhone());

        try {
            // Creating a big string of everything the user filled in
            String allUserInfo =
                    trueYouUser.getFirstName() +
                    trueYouUser.getLastName() +
                    trueYouUser.getDescription() +
                    trueYouUser.getPhone() +
                    trueYouUser.getEmail() +
                    trueYouUser.getCountry() +
                    trueYouUser.getDateOfBirth() +
                    trueYouUser.getResidence() +
                    publicKey;

            Authenticator authenticator = new Authenticator(context);
            // Create digital signature
            String digiSig = authenticator.registerDigiSig(allUserInfo);
            // Create a hash of the public key with SHA256
//            String hashedPubKey = Hasher.hashWithSHA256(publicKey);
            // Create a hash of the user string with SHA256
//            String hashedUser = Hasher.hashWithSHA256(allUserInfo);
            // Store stuff in ArrayList to send with POST request
            hashKeyList.put(1, publicKey);
            hashKeyList.put(2, digiSig);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return hashKeyList;
    }

    // Check if email is valid
    public boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    // Check if date of birth is valid
    public boolean validDateOfBirth(String dateOfBirth) {
        return dateOfBirth.matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))");
    }

    // Callback method after trueYouUser has been created
    @Override
    public void onTrueYouUserAvailable(TrueYouUser trueYouUser) {
        firstname.getText().clear();
        prefix.getText().clear();
        lastname.getText().clear();
        description.getText().clear();
        residence.getText().clear();
        country.getText().clear();
        dateofbirth.getText().clear();
        email.getText().clear();
        phone.getText().clear();
        btnRegister.setEnabled(false);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("nl.avans.android.circle.SHARED_PREFS_FILE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("account_created", true);
        editor.apply();

        String accountIsAdded = "Account has been added. Please go to The Circle";
        Toast.makeText(getApplicationContext(), accountIsAdded, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onTrueYouUserError(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        try {
            if(message.contains("This emailaddress is")) {
                Toast.makeText(getApplicationContext(), "This emailaddress is already in use. Please use a different one.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
        }
    }
}
