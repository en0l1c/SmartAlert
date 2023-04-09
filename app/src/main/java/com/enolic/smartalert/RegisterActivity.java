package com.enolic.smartalert;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity implements LocationListener{

    Button signUpButton;
    EditText emailET;
    EditText passwordET;
    EditText fullNameET;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    //private User user;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    private LocationManager locationManager;
    private Location currentLocation;
    private String provider;
    private final int LOCATION_REQUEST_CODE = 123;

    double lat;
    double lng;



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



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Get location service
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.

                                checkPermAndGetLocation();


                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.


                            } else {
                                // No location access granted.


                            }
                        }
                );

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });


        // SIGN UP
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (lat != 0 && lng != 0) {
                    registerUser();
                } else {
                    Toast.makeText(RegisterActivity.this, "wait for locationf", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // need to check if requestCode is the same as LOCATION_REQUEST_CODE

        // ksanavazoume to check gia to permission stin onrequestpermissionresult wste na min xreiastei na patithei duo fores to koumpi gia na travixei to location

        checkPermAndGetLocation();



    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();


        locationManager.removeUpdates(this); // this stops the requestLocationUpdates
    }
    private void checkPermAndGetLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }



    public void registerUser() {

        if(!emailET.getText().toString().equals("") && !passwordET.getText().toString().equals("") && !fullNameET.getText().toString().equals("")) {
            mAuth.createUserWithEmailAndPassword(emailET.getText().toString(), passwordET.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // to ti exei sumbei, an exei ginei kapoio lathos kata tin eisagwgi twn stoixeiwn vrisketai entos tou task
                            if(task.isSuccessful()) {

                                Toast.makeText(RegisterActivity.this, "Success registration", Toast.LENGTH_SHORT).show();

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

                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful()) {
                                                    String fcmToken = task.getResult();

                                                    writeNewUserToDatabase(mAuth.getUid(),
                                                            emailET.getText().toString(),
                                                            fullNameET.getText().toString(),
                                                            lat,
                                                            lng,
                                                            fcmToken);
                                                } else {
                                                    Log.d("MyApp", "Failed to get FCM token: " + task.getException().getMessage());
                                                }
                                            }
                                        });

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

    public void writeNewUserToDatabase(String userId, String email, String name, double lat, double lng, String fcmToken) {
        User user = new User(email, name, lat, lng, fcmToken);
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































