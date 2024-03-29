package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LoginActivity extends AppCompatActivity {
    Alert alert;
    EditText emailET;
    EditText passwordET;
    public static String uid;

    static FirebaseAuth mAuth;
    static FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference referece;
    DatabaseReference roleReference;
    Switch languageSwitch;
    Button signUpButton;
    Button signInButton;
    ArrayList<Double> lats = new ArrayList<>(); // latitudes
    ArrayList<Double> lngs = new ArrayList<>(); // longtides
    ArrayList<Integer> timestamps = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();
    ArrayList<String> users = new ArrayList<>();

    // these two variables will be used by SharedPreferences
    private static final String FILE_NAME = "file_lang"; // preference file name
    private static final String KEY_LANG = "key_lang"; // preference key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // please load language after super and before setContentView
        loadLanguage();
        setContentView(R.layout.activity_login);


        database = FirebaseDatabase.getInstance();
        referece = database.getReference("myMessage"); // path ena meros tou dentrou tis mi sxesiakis vasis




        languageSwitch = findViewById(R.id.languageSwitch);
        signUpButton = findViewById(R.id.registerBtn);
        signInButton = findViewById(R.id.loginBtn);
        emailET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);


//        if(mAuth != null) {
//            mAuth.signOut(); // signout first to make sure that authentication system is secure, because we are at onCreate of login activity.
//
//        }



        mAuth = FirebaseAuth.getInstance();

//        uid = mAuth.getUid();

        compareAlerts();
        //compareAlertsLogin();
        passValuesToLists();

        //showPermissionDialog("permissions", "this is the message");


        // take saved pref for language switch, and toogle it or not
        SharedPreferences sharedPrefs = getSharedPreferences("com.enolic.smartalert", MODE_PRIVATE);
        languageSwitch.setChecked(sharedPrefs.getBoolean("isLangSwitchChecked", true));

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                if(!emailET.getText().toString().equals("") || !passwordET.getText().toString().equals("")) {
                    intent.putExtra("EMAIL_KEY", emailET.getText().toString());
                    intent.putExtra("PASSWORD_KEY", passwordET.getText().toString());
                }
                startActivity(intent);
//                finish();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                singInTheUser();
//                if(user != null) {
//
//                }
//                else {
//
//                    Toast.makeText(LoginActivity.this, "error / you are not a valid user", Toast.LENGTH_SHORT).show();
//                }



            }
        });
    }

    private void singInTheUser() {
        user = mAuth.getCurrentUser();
        // PUT IT IN A TRY-CATCH IF THE EDITTEXTS ARE NULL
        try {
            mAuth.signInWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
                    .addOnCompleteListener((task) -> { // with lambda expression
                        // to ti exei sumbei, an exei ginei kapoio lathos kata tin eisagwgi twn stoixeiwn vrisketai entos tou task
                        if (task.isSuccessful()) {
                            roleReference = database.getReference().child("USER").child(mAuth.getUid()).child("role");
                            roleReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    try {
                                        String userRole = snapshot.getValue().toString();

                                        if(userRole.equals("admin")) {

                                            startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                                            finish();
                                            Toast.makeText(LoginActivity.this, "Admin authenticated", Toast.LENGTH_SHORT).show();                                                }
                                        else {

//                                                    emailET.getText().clear();
//                                                    passwordET.getText().clear();

                                            startActivity(new Intent(LoginActivity.this, UserActivity.class));
                                            finish();
                                            Toast.makeText(LoginActivity.this, "User authenticated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    catch(Exception e) {
                                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();

                                }
                            });



                        } else {
                            // den egine me epituxia
                            showMessage("Error", task.getException().getLocalizedMessage()); // epistrefetai to localized message oxi to exception. an epestrefai to exception tha itan epikinduno gia keno asfaleias
                        }

                    });
        }
        catch (Exception e) {
            showMessage("Error", "Fill carefully the fields");
        }
    }
    private void saveLanguage(String lang) {


        // we can use this method to save language
        SharedPreferences preferences = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANG, lang);
        editor.apply();
        // we have saved
        // recreate activity after saving to load the new language, this is the same
        // as refreshing activity to load new language

        recreate();

    }

    private void loadLanguage() {
        // we can use this method to load language,
        // this method should be called before setContentView() method of the onCreate method

        Locale locale = new Locale(getLangCode());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private String getLangCode() {
        SharedPreferences preferences = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        String langCode = preferences.getString(KEY_LANG, "en");
        // save english 'en' as the default language
        return langCode;
    }


    // change language with click on switch (onClickListener)
    public void changeLanguageSwitch(View view) {


        if(languageSwitch.isChecked()) {
            saveLanguage("en");
            SharedPreferences.Editor editor = getSharedPreferences("com.enolic.smartalert", MODE_PRIVATE).edit();
            editor.putBoolean("isLangSwitchChecked", true);
            editor.commit();
            Toast.makeText(this, "Switched to English", Toast.LENGTH_SHORT).show();
        }
        else {
            saveLanguage("el");
            SharedPreferences.Editor editor = getSharedPreferences("com.enolic.smartalert", MODE_PRIVATE).edit();
            editor.putBoolean("isLangSwitchChecked", false);
            editor.commit();
            Toast.makeText(this, "Μετάβαση σε Ελληνικά", Toast.LENGTH_SHORT).show();
        }
    }

    public void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Here you want to show the user a dialog box
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.exitingFromApp)
                .setMessage(R.string.areYouSure)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user wants to leave - so dismiss the dialog and exit
                        finish();
                        dialog.dismiss();
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "You signed out from loginActivity", Toast.LENGTH_SHORT).show();
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user is not sure, so you can exit or just stay
                        dialog.dismiss();
                    }
                }).show();

    }



    private void passValuesToLists() {


        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("ALERT");


        dataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Alert alert = snapshot.getValue(Alert.class);


//                Toast.makeText(LoginActivity.this, snapshot.getKey(), Toast.LENGTH_SHORT).show();


                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);

                    lats.add(alert.getLat());
                    lngs.add(alert.getLng());
                    timestamps.add(alert.getTimestamp());
                    categories.add(alert.getCategory());
                    users.add(alert.getUserId());

                    //Toast.makeText(LoginActivity.this, "for: " + dataSnapshot.child("location").getValue(String.class), Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(LoginActivity.this, " " + lats.size() + " " + lngs.size() + " " + timestamps.size() + " " + categories.size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(LoginActivity.this, "alertTitle: " + alert.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        compareAlerts();
    }

    private void compareAlerts() {


        DatabaseReference alertsRef = FirebaseDatabase.getInstance().getReference("ALERT");
        alertsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Loop through all the alerts
                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                    String alertUid = alertSnapshot.child("userId").getValue(String.class);
                    long alertTimestamp = alertSnapshot.child("timestamp").getValue(long.class);
                    double alertLatitude = alertSnapshot.child("lat").getValue(double.class);
                    double alertLongitude = alertSnapshot.child("lng").getValue(double.class);
                    String alertCategory = alertSnapshot.child("category").getValue(String.class);
                    int alertDanger = alertSnapshot.child("danger").getValue(Integer.class);

                    // Check if the alert was submitted within the last 4 hours
                    long now = System.currentTimeMillis() / 1000;
                    if (now - alertTimestamp <= 4 * 60 * 60 ) {
                        // Check if the alert's danger level is not already 3
                        if (alertDanger != 3) {
                            // Loop through all the alerts again to find other alerts with the same category and nearby location
                            int nearbyAlerts = 0;
                            for (DataSnapshot otherAlertSnapshot : snapshot.getChildren()) {
                                String otherAlertUid = otherAlertSnapshot.child("userId").getValue(String.class);
                                double otherAlertLatitude = otherAlertSnapshot.child("lat").getValue(double.class);
                                double otherAlertLongitude = otherAlertSnapshot.child("lng").getValue(double.class);
                                String otherAlertCategory = otherAlertSnapshot.child("category").getValue(String.class);
                                int otherAlertDanger = otherAlertSnapshot.child("danger").getValue(Integer.class);

                                // Check if the other alert was submitted within the last 4 hours
                                long otherAlertTimestamp = otherAlertSnapshot.child("timestamp").getValue(long.class);
                                if (now - otherAlertTimestamp <= 4 * 60 * 60) {
                                    // Check if the other alert has the same category and nearby location
                                    if (!alertUid.equals(otherAlertUid) && alertCategory.equals(otherAlertCategory)) {
                                        float[] results = new float[1];
                                        Location.distanceBetween(alertLatitude, alertLongitude, otherAlertLatitude, otherAlertLongitude, results);
                                        if (results[0] <= 1000) {
                                            // We have found a nearby alert with the same category and within the last 4 hours
                                            nearbyAlerts++;

                                            if (nearbyAlerts >= 4) {
                                                // If there are at least 5 nearby alerts with the same category within the last 4 hours, set their danger level to 3
                                                alertsRef.child(alertSnapshot.getKey()).child("danger").setValue(3);
                                                alertsRef.child(otherAlertSnapshot.getKey()).child("danger").setValue(3);
                                                // You can continue to set the danger level of other nearby alerts to 3 as well, depending on your needs
                                                break;
                                            }
                                            else {
                                                alertsRef.child(alertSnapshot.getKey()).child("danger").setValue(2);
                                                alertsRef.child(otherAlertSnapshot.getKey()).child("danger").setValue(2);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });

    }

    public void showPermissionDialog(String title, String message) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);



//        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNeutralButton("Neutral button default text", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        String neutralBtnText = "Mark As important";

    }



}
















































