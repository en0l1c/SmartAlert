package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    Alert alert;
    EditText emailET;
    EditText passwordET;
    public static String uid;

    FirebaseAuth mAuth;
    FirebaseUser user;
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


        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();

        //compareAlertsLogin();
        passValuesToLists();

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
                finish();
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
//                                                    // Clear the edit texts
//                                                    emailET.getText().clear();
//                                                    passwordET.getText().clear();

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
//    View.OnClickListener listener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            // sign in button
//            if(view == signInButton) {
//                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
//                showMessage("Success", "onclick outside of the oncreate");
//            }
//        }
//    };


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
                    timestamps.add(Integer.parseInt(alert.getTimestamp()));
                    categories.add(alert.getCategory());

                    //Toast.makeText(LoginActivity.this, "for: " + dataSnapshot.child("location").getValue(String.class), Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(LoginActivity.this, " " + lats.size() + " " + lngs.size() + " " + timestamps.size() + " " + categories.size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(LoginActivity.this, "alertTitle: " + alert.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        compareAlerts();
    }

    private void compareAlerts() {

        int cnt = 0;
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("ALERT");

        dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullList = "";
                // for every location on lats and lngs make a for loop to look for all other locations and match the neariest locations

                int index = 0;
                int sameAlertCnt = 1;

                while(index < lats.size()) {
                    // Location
                    double latFromList = lats.get(index);
                    double lngFromList = lngs.get(index);
//                    double latFromList = 38.00356076; // uniwa
//                    double lngFromList = 23.67547215;

                    // Timestamp
                    int tsFromList = timestamps.get(index);
                    // Category
                    String catFromList = categories.get(index);

                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);

                        // Location
                        double latCurrent = dataSnapshot.child("lat").getValue(Double.class);
                        double lngCurrent = dataSnapshot.child("lng").getValue(Double.class);
//                        double latCurrent = alert.getLat(); // current lat from for-loop reading the realtime database
//                        double lngCurrent = alert.getLng();

                        // Timestamp
                        int tsCurrent = Integer.parseInt(alert.getTimestamp());
                        // Category
                        String catCurrent = alert.getCategory();


                        double distance = AdminActivity.calcDistance(latFromList, lngFromList, latCurrent, lngCurrent);
                        boolean isTimeValid = AdminActivity.isTimeValid(tsFromList, tsCurrent);
                        boolean isCategoryValid = Objects.equals(catFromList, catCurrent);

                        if(distance < 0.9 && isTimeValid && isCategoryValid) {

                            if(sameAlertCnt >= 6) {
                                fullList += "\n" + index + ": " + alert.getTitle() + " | Danger: 3";
                            }
                            else if(sameAlertCnt >= 3 && sameAlertCnt <=5) {
                                fullList += "\n" + index + ": " + alert.getTitle() + " | Danger: 2";
                            }
                            else {
                                fullList += "\n" + index + ": " + alert.getTitle() + " | Danger: 1";
                            }
                            sameAlertCnt++;
                        }

                        //Toast.makeText(LoginActivity.this, String.valueOf("Lat from snaps: " + latCurrent), Toast.LENGTH_SHORT).show();
                        //Toast.makeText(LoginActivity.this, String.valueOf(distance), Toast.LENGTH_SHORT).show();


                    }
                    sameAlertCnt = 1;
                    fullList += "\n";
                    index++;
                }
                showMessage("Near spots: ", fullList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



//        dataRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String fullList = "";
//                // for every location on lats and lngs make a for loop to look for all other locations and match the neariest locations
//
//                for(int i = 0; i < lats.size() - 1; i++) {
////                    double latFromList = lats.get(i);
////                    double lngFromList = lngs.get(i);
//                    double latFromList = 38.00356076; // uniwa
//                    double lngFromList = 23.67547215;
//
//                    //Toast.makeText(LoginActivity.this, "Lat from list: " + String.valueOf(latFromList), Toast.LENGTH_SHORT).show();
//
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
//
//                        Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);
//
//                        double latCurrent = dataSnapshot.child("lat").getValue(Double.class);
//                        double lngCurrent = dataSnapshot.child("lng").getValue(Double.class);
////                        double latCurrent = alert.getLat(); // current lat from for-loop reading the realtime database
////                        double lngCurrent = alert.getLng();
//
//                        double distance = AdminActivity.calcDistance(latFromList, lngFromList, latCurrent, lngCurrent);
//
//                        if(distance < 0.9) {
//
//                            fullList += "\n" + alert.getTitle();
//                        }
//
//                        //Toast.makeText(LoginActivity.this, String.valueOf("Lat from snaps: " + latCurrent), Toast.LENGTH_SHORT).show();
//                        //Toast.makeText(LoginActivity.this, String.valueOf(distance), Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//                showMessage("Near spots: ", fullList);
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

        //Toast.makeText(LoginActivity.this, cnt, Toast.LENGTH_SHORT).show();
    }

//    private void compareAlertsLogin() {
//        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("ALERT");
//        //listView = findViewById(R.id.listView);
//
//
//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//
//
//
//
//                // for check nearby locations
//                dataRef.addValueEventListener(new ValueEventListener() {
//                    String categoryLast;
//                    String categoryCurrent;
//
//                    String timestampLast;
//                    String timestampCurrent;
//
//                    double latCurrent = 0;
//                    double lngCurrent = 0;
//                    double latLast;
//                    double lngLast;
//                    int userCounter = 0;
//                    double distance;
//                    String titleLast;
//
//
//                    HashMap hashMap = new HashMap<String, String>();
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        // edw tha parei to location apo to antistoixo item tis realtime database kai meta stin for parakatw tha sugkrinetai to kathe location me autoj
//
//                        Alert alert = snapshot.getValue(Alert.class);
//                        Toast.makeText(LoginActivity.this, "alertTitle: " + alert.getTitle(), Toast.LENGTH_SHORT).show();
//
//
//
//                        // get the first location and with for check all other locations if nearby
//                        try {
//                            latLast = alert.getLat();
//                            lngLast = alert.getLng();
//                            titleLast = alert.getTitle();
//
//                            Toast.makeText(LoginActivity.this, "latLast: " + latLast, Toast.LENGTH_SHORT).show();
//
//                            timestampLast = alert.getTimestamp();
//                            categoryLast = alert.getCategory();
//                        }
//                        catch(Exception e) {
//
//                        }
//
////                Toast.makeText(AdminActivity.this, "latLast: " + latLast, Toast.LENGTH_SHORT).show();
//
//
//                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                            Alert alrt = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);
//
//                            // save last location and current location to compare them on isLocationValid method
//
//
//                            try {
//
//
//                                latCurrent = alrt.getLat();
//                                lngCurrent = alrt.getLng();
//
//                                Toast.makeText(LoginActivity.this, "latCurrent: " + latCurrent, Toast.LENGTH_SHORT).show();
//
//                                //boolean isValidAlert = true;
//
//                                // save last timestamp and current timestamp to compare them on isValidTime method
//                                //timestampLast = timestampCurrent; // time when alert submitted
//                                timestampCurrent = alrt.getTimestamp();
//
//                                //timestampCurrent = String.valueOf(System.currentTimeMillis() / 1000); // current time
//
//
//                                // categories
//                                //categoryLast = categoryCurrent;
//                                categoryCurrent = alrt.getCategory();
//
//
//
//                                distance = AdminActivity.calcDistance(latLast, lngLast, latCurrent, lngCurrent);
////                                showMessage("", "First Location: \n" +
////                                    "latCurrent: " + latLast + "," + lngLast +
////                                    "\nLocationTitle: " + titleLast +
////                                    "\ndisatnce: " + distance +
////                                    "\n\nSecond Location: \n" +
////                                    "latCurrent: " + latCurrent + "," + lngCurrent +
////                                    "\nLocationTitle: " + alert.getTitle() +
////                                    "\ndisatnce: " + distance);
//
//
//
//
////                    if(distance < 0.9 && Objects.equals(categoryLast, categoryCurrent) && isTimeValid(timestampLast, timestampCurrent))
//                                if(distance < 0.9){
//                                    //nearbyLocations.add(alert.getTitle());
////                        if there is more than 6 alerts on the same location the dangerous level goes to 3
//                                    if(userCounter >= 6) {
//                                        if(AdminActivity.isTimeValid(timestampLast, timestampCurrent) && Objects.equals(categoryLast, categoryCurrent)) {
//                                            hashMap.put("danger", "3");
//                                            dataRef.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                                        }
//                                        else {
//                                            hashMap.put("danger", "22");
//                                            dataRef.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                                        }
//
//                                    }
//                                    else {
//                                        hashMap.put("danger", "2");
//                                        dataRef.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                                    }
//                                    userCounter++;
//
//                                }
//                                else {
//                                    hashMap.put("danger", "1");
//                                    dataRef.child(dataSnapshot.getKey()).updateChildren(hashMap);
////                        Toast.makeText(AdminActivity.this, alert.getTitle() + " false", Toast.LENGTH_SHORT).show();
//
//                                }
//
//
//
//                            }
//                            catch(Exception e) {
//
//                            }
//
//
//
//
//                        }
//                        userCounter = 0;
//                        latCurrent = 0;
//                        lngCurrent = 0;
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//        });
//    }










}
















































