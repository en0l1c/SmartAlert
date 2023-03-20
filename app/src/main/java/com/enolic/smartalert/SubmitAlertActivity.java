package com.enolic.smartalert;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubmitAlertActivity extends AppCompatActivity implements LocationListener {

    EditText alertTitileET;
    TextView timestampTV;
    TextView locationTV;
    TextView imageSelectedTV;
    EditText alertDescriptionET;

    Button refreshTimestampBtn;
    Button refreshGpsBtn;
    Button selectImageBtn;
    Button submitAlertBtn;
    Spinner alertCategorySpinner;
    LocationManager locationManager;

    UploadTask uploadTask;
    Uri imageUri;
    private String fileName; // image file name
//    private SimpleDateFormat formatter;
//    private Date dateNow;
    String downloadUrl;
    Uri downloadUri;
    String forNullUserIdBug;
    StorageReference storageReference;
    double lat;
    double lng;


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    private final int LOCATION_REQUEST_CODE = 123;

    ArrayList<String> users = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_alert);

        alertTitileET = findViewById(R.id.alertTitleET);
        timestampTV = findViewById(R.id.timestampTV);
        locationTV = findViewById(R.id.gpsTV);
        alertDescriptionET = findViewById(R.id.alertDescriptionET);
        refreshGpsBtn = findViewById(R.id.refreshGpsBtn);
        refreshTimestampBtn = findViewById(R.id.refreshTsBtn);
        alertCategorySpinner = findViewById(R.id.alertCategorySpinner);
        selectImageBtn = findViewById(R.id.selectImgBtn);
        submitAlertBtn = findViewById(R.id.submitAlertBtn);
        imageSelectedTV = findViewById(R.id.imgSelectedTV);

        imageSelectedTV.setTextColor(Color.RED);

        locationTV.setTextColor(Color.RED);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Long tsLong = System.currentTimeMillis() / 1000; //timestamp
        timestampTV.setText(tsLong.toString());


        mAuth = FirebaseAuth.getInstance();
        forNullUserIdBug = mAuth.getUid();

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("ALERT");


        // Refresh timestamp at clicking the refresh button

        refreshTimestampBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshTimestampAndGps();
            }
        });

        refreshGpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshTimestampAndGps();
            }
        });

        // SELECT IMAGE BUTTON
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });







//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//        // request location
//
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        //locationManager.removeUpdates(this); // this stops the requestLocationUpdates
//
//        // kanoume implement ton listener wste na ananeonoume sunexos to location tou locationManager.requestLocationUpdates
//        // tha valoume tin idia tin activity na kanei implement ton LocationListener



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









        // TODO: let the user know if he has selected the image with some textview
        // TODO: submitButton doesnt work on first time android as for location permission




        // SUBMIT BUTTON
        submitAlertBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

//                canUserSubmitAlert();

                if(!alertTitileET.getText().toString().equals("") &&
                        !alertDescriptionET.getText().toString().equals("") &&
                        !timestampTV.getText().toString().equals("") &&
                        locationTV.getCurrentTextColor() != Color.RED &&
                        locationTV.getCurrentTextColor() != Color.YELLOW &&
                        !alertCategorySpinner.getSelectedItem().toString().equals("-")) {




                            // with image
                            if(imageUri != null) {
                                uploadImage(); // firstly upload the image


                                // continue to upload task from uploadImage() and get the downloadUrl of image
                                // see here why i did this way:
                                // https://stackoverflow.com/questions/68241801/why-cant-this-retieve-the-download-url-of-an-image-on-firebase-storage
                                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if(!task.isSuccessful()) {
                                            throw task.getException();

                                        }

                                        return storageReference.getDownloadUrl();

                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()) {
                                            downloadUri = task.getResult();


                                            if(mAuth.getUid() != null) {
                                                writeNewAlertToDatabase(alertTitileET.getText().toString(),
                                                        timestampTV.getText().toString(),
                                                        locationTV.getText().toString(),
                                                        alertDescriptionET.getText().toString(),
                                                        alertCategorySpinner.getSelectedItem().toString(),
                                                        downloadUri.toString(),
                                                        forNullUserIdBug,
                                                        lat,
                                                        lng,
                                                        0,
                                                        false);
                                            }
                                            else {
                                                Toast.makeText(SubmitAlertActivity.this, "this if is a test for null uid (with img)", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    }
                                });



                                Toast.makeText(SubmitAlertActivity.this, "succesfully wrote to db with img", Toast.LENGTH_SHORT).show();
                            }

                            // without image
                            else {
                                if(mAuth.getUid() != null) {
                                    // without uploading image with
                                    writeNewAlertToDatabase(alertTitileET.getText().toString(),
                                            timestampTV.getText().toString(),
                                            locationTV.getText().toString(),
                                            alertDescriptionET.getText().toString(),
                                            alertCategorySpinner.getSelectedItem().toString(),
                                            "",
                                            mAuth.getUid(),
                                            lat,
                                            lng,
                                            0,
                                            false);


                                    Toast.makeText(SubmitAlertActivity.this, "succesfully wrote to db without img", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(SubmitAlertActivity.this, "this if is a test for null uid (without img)", Toast.LENGTH_SHORT).show();
                                }

                            }


                            startActivity(new Intent(SubmitAlertActivity.this, UserActivity.class));
                            finish();

                }else {
                    Toast.makeText(getApplicationContext(), "Some info missing", Toast.LENGTH_SHORT).show();
                }


            }

        });
    }

    public void writeNewAlertToDatabase(String title,
                                        String timestasmp,
                                        String location,
                                        String description,
                                        String category,
                                        String image,
                                        String userId,
                                        double lat,
                                        double lng,
                                        int dangerLevel,
                                        boolean isVerified) {

        Alert alert = new Alert(title,
                timestasmp,
                location,
                description,
                category,
                image,
                userId,
                lat,
                lng,
                dangerLevel,
                isVerified);
        databaseReference.child(title).setValue(alert);
    }

    private void canUserSubmitAlert() {
        boolean canUserSubmit;
        String uid = mAuth.getUid();
        int timestampCurrent = (int) System.currentTimeMillis() / 1000; // current time


        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            int cnt = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);

                    if(uid.equals(alert.getUserId()))
                    {

                        cnt++;

                    }

                }
                Toast.makeText(SubmitAlertActivity.this, cnt, Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    // otan kleisei to parathyro pou zitaei to permission apo ton xristi tote tha treksei o kodikas tis onRequestPermissionResult
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
        // set gps location at onCreate of activity
        locationTV.setText(location.getLatitude() + "," + location.getLongitude());
        locationTV.setTextColor(Color.GREEN);

        locationManager.removeUpdates(this); // this stops the requestLocationUpdates
    }


    private void refreshTimestampAndGps() {
        // Method to refresh timestamp and gps location
        Toast.makeText(this, "Timestamp and Location updated", Toast.LENGTH_SHORT).show();
        // Timestamp
        Long tsLong = System.currentTimeMillis() / 1000; //timestamp
        timestampTV.setText(tsLong.toString());

        // GPS
        //generate once again the lat and long when refresh button is clicked

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

        checkPermAndGetLocation();

        locationTV.setTextColor(Color.YELLOW);
    }

    private void checkPermAndGetLocation() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }




    // FOR INTENT TO OPEN PHONE GALLERY
    // BECAUSE startActivityForResult is depraced
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent data = result.getData(); // selected media (image)

            // or check if result.getResultCode == 100
            if(result.getResultCode() == Activity.RESULT_OK && data != null && data.getData() != null) {
                // there are no request codes
                String imagePath = data.getData().getPath();
                imageUri = data.getData();

                if(imagePath.contains("/image")) {
                    imageSelectedTV.setVisibility(View.VISIBLE);
                    imageSelectedTV.setText("Image Selected.");
                    imageSelectedTV.setTextColor(Color.GREEN);
                    Toast.makeText(SubmitAlertActivity.this, "image selected" + imageUri.toString(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SubmitAlertActivity.this, "there is no image selected", Toast.LENGTH_SHORT).show();
                }
                

            }
        }
    });
    private void selectImage() {


        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        someActivityResultLauncher.launch(intent);




        //startActivityForResult(intent,100);
    }


    private void uploadImage() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        fileName = formatter.format(now);


        storageReference = FirebaseStorage.getInstance().getReference("images/").child(fileName);




        uploadTask = storageReference.putFile(imageUri);




    }

    private void getUrlImage() {

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()) {
                    throw task.getException();

                }

                return storageReference.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()) {
                    downloadUri = task.getResult();
                    downloadUrl = task.getResult().toString();
                    Toast.makeText(SubmitAlertActivity.this, "Success: " + downloadUri.toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(SubmitAlertActivity.this, "Download URL: " + downloadUrl, Toast.LENGTH_SHORT).show();

                }

                downloadUrl = downloadUri.toString();
            }

        });
        Toast.makeText(SubmitAlertActivity.this, "Download URL (out2): " + downloadUrl, Toast.LENGTH_SHORT).show();


        // downloadUrl;
    }








//    public void showMessage(String title, String message) {
//        new android.app.AlertDialog.Builder(this)
//                .setTitle(title)
//                .setMessage(message)
//                .setCancelable(true)
//                .show();
//    }

    @Override
    public void onBackPressed() {
        // CHECK IF THE FIELDS ARE NOT EMPTY AND PROMPT USER TO NOT GO BACK


        // Here you want to show the user a dialog box
        if(!alertTitileET.getText().toString().equals("") ||
                !alertDescriptionET.getText().toString().equals("") ||
                !alertCategorySpinner.getSelectedItem().toString().equals("-")
                )
        {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.signingOut)
                    .setMessage(R.string.areYouSure)
                    .setPositiveButton("ΝΑΙ", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // The user wants to leave - so dismiss the dialog and exit
                            dialog.dismiss();
                            //Toast.makeText(SubmitAlertActivity.this, "You signed out from UserActivity", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SubmitAlertActivity.this, UserActivity.class));
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
            startActivity(new Intent(SubmitAlertActivity.this, UserActivity.class));
            finish();
        }
    }

}






















