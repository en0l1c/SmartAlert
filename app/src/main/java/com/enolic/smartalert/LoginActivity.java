package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
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


}













