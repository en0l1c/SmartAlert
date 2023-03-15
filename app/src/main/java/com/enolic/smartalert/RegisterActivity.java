package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    Button signUpButton;
    EditText emailET;
    EditText passwordET;
    EditText fullNameET;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    //private User user;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpButton = findViewById(R.id.registerBtn);
        emailET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        fullNameET = findViewById(R.id.fullnameET);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("USER"); // path ena meros tou dentrou tis mi sxesiakis vasis

        // Get email and password from login activity if user had submitted info
        emailET.setText(getIntent().getStringExtra("EMAIL_KEY"));
        passwordET.setText(getIntent().getStringExtra("PASSWORD_KEY"));

        // SIGN UP
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!emailET.getText().toString().equals("") && !passwordET.getText().toString().equals("") && !fullNameET.getText().toString().equals("")) {
                    mAuth.createUserWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    // to ti exei sumbei, an exei ginei kapoio lathos kata tin eisagwgi twn stoixeiwn vrisketai entos tou task
                                    if(task.isSuccessful()) {
                                        showMessage("Success", "User authenticated");

                                        // Set full name / display name
                                        firebaseUser = mAuth.getCurrentUser();


                                        // UPDATE PROFILE DISPLAY NAME

                                        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullNameET.getText().toString())
                                                .build();

                                        firebaseUser.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(RegisterActivity.this, "User profile updated  ", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                        writeNewUserToDatabase(mAuth.getUid(), emailET.getText().toString(), fullNameET.getText().toString());

                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                    else {
                                        // den egine me epituxia
                                        showMessage("Error", task.getException().getLocalizedMessage()); // epistrefetai to localized message oxi to exception. an epestrefai to exception tha itan epikinduno gia keno asfaleias
                                    }
                                }
                            });
                }else {
                    Toast.makeText(RegisterActivity.this, "Fill carefully all the fields", Toast.LENGTH_SHORT).show();
                }

                
                }

                

        });

    }

    public void writeNewUserToDatabase(String userId, String email, String name) {
        User user = new User(email, name);
        databaseReference.child(userId).setValue(user);
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
        // CHECK IF THE FIELDS ARE NOT EMPTY AND PROMPT USER TO NOT GO BACK


        // Here you want to show the user a dialog box
        if(!emailET.getText().toString().equals("") ||
                !passwordET.getText().toString().equals("") ||
                !fullNameET.getText().toString().equals("")
        )
        {

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.signingOut)
                    .setMessage(R.string.areYouSure)
                    .setPositiveButton("ΝΑΙ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // The user wants to leave - so dismiss the dialog and exit
                            dialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();

                        }
                    }).setNegativeButton("ΟΧΙ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // The user is not sure, so you can exit or just stay
                            dialog.dismiss();
                        }
                    }).show();

        }
        else {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();

        }
    }
}































