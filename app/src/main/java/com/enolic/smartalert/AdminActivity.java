package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.annotation.SuppressLint;
import android.app.LauncherActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    ListView listView;
    ArrayList<String> list = new ArrayList<>();
    ArrayAdapter<String> adapter;
    StorageReference storageRef;
    ImageView imgView;
    ArrayList<Integer> dangerPos = new ArrayList<Integer>();
    ArrayList<String> dangerPosByTs = new ArrayList<String>();
    ArrayList<String> nearbyLocations = new ArrayList<String>();
    ArrayList<String> whichDanger3 = new ArrayList<>();

    int listViewSelectedItem;

    Alert alert;
    Toolbar toolbar;

    //String timestampLast = "0";
    //String timestampCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        listView = findViewById(R.id.listView);

        mAuth = FirebaseAuth.getInstance();
        //database = FirebaseDatabase.getInstance();
        //databaseReference = database.getReference("ALERTS");


        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Less dangerous alerts");
        setSupportActionBar(toolbar);



        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);



        listView.setAdapter(adapter);







        // get the reference from firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference("ALERT");

        addDataToList();



        //  ON CLICK LISTENER FOR LISTVIEW ITEM
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {



                final String selectedFromList = (String) listView.getItemAtPosition(position);

                databaseReference.orderByChild(selectedFromList).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Alert alert = snapshot.child(selectedFromList).getValue(Alert.class);

//                        String dangerLevel = snapshot.child(selectedFromList).child("danger").getValue(Integer.class);


                        showAndManageAlert(alert.getTitle(),
                                alert.getCategory(),
                                alert.getDescription(),
                                alert.getLocation(),
                                alert.getTimestamp(),
                                alert.getUserId(),
                                alert.getImage(),
                                alert.getLat(),
                                alert.getLng(),
                                alert.getDanger(),
                                alert.isVerified(),
                                snapshot,
                                selectedFromList);


                        Toast.makeText(AdminActivity.this, "Position at ListView: " + String.valueOf(position), Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




            }

        });




    }





    //https://stackoverflow.com/questions/45088995/how-do-i-get-a-list-of-nearby-users-in-an-android-application-efficiently
    //https://stackoverflow.com/questions/56301510/can-i-change-the-order-of-children-in-firebase
    //https://console.firebase.google.com/u/0/project/smartalert-1337/config
    //https://stackoverflow.com/questions/2217753/changing-background-color-of-listview-items-on-android
    //https://stackoverflow.com/questions/32393134/getting-results-of-nearby-places-from-users-location-using-google-maps-api-in-a

    static public double calcDistance(double latLast, double lngLast, double latCurrent, double lngCurrent) {

        // check distance
        double earthRadius = 6371; // in kilometers

        double dLat = Math.toRadians(Math.abs(latCurrent - latLast));
        double dLng = Math.toRadians(Math.abs(lngCurrent - lngLast));

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(latCurrent)) * Math.cos(Math.toRadians(latLast));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;

    }
    static public boolean isTimeValid(int timestampLast,
                          int timestampCurrent) {
        try {
            // firstly convert string epoch to number for comparison

            int tsLast = timestampLast;
            int tsCurrent = timestampCurrent;
//            int tsLast = Integer.parseInt(timestampLast);
//            int tsCurrent = Integer.parseInt(timestampCurrent);


            // calculate the time (in seconds) difference between two epochs
            int timeDifference = Math.abs(tsCurrent - tsLast);

            // check if the difference of time (in seconds) between the alerts is near by

            // 4 hours check
            if(timeDifference < 14400) {
                //Toast.makeText(this, "less than hour", Toast.LENGTH_SHORT).show();

                return true;
            }
            else {
                //Toast.makeText(this, "more than hour", Toast.LENGTH_SHORT).show();

                return false;

            }
        }
        catch(Exception e) {

        }
        return false;
    }

    static public int isTimeValid_Int(int timestampLast,
                                      int timestampCurrent) {
        // firstly convert string epoch to number for comparison

        int tsLast = timestampLast;
        int tsCurrent = timestampCurrent;
//            int tsLast = Integer.parseInt(timestampLast);
//            int tsCurrent = Integer.parseInt(timestampCurrent);


        // calculate the time (in seconds) difference between two epochs
        int timeDifference = Math.abs(tsCurrent - tsLast);

        // check if the difference of time (in seconds) between the alerts is near by

        // 4 hours check
        if(timeDifference < 14400) {
            //Toast.makeText(this, "less than hour", Toast.LENGTH_SHORT).show();

            return timeDifference;
        }
        else {
            //Toast.makeText(this, "more than hour", Toast.LENGTH_SHORT).show();

            return timeDifference;

        }
    }


    private void updateListItemBackground() {



        for(int i = 0; i < list.size(); i++) {
            int index = i;
            String listItemText = list.get(i);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String dangerLevel = dataSnapshot.child("danger").getValue(String.class);
                        Toast.makeText(AdminActivity.this, "danger: " + dangerLevel, Toast.LENGTH_SHORT).show();

                        if(dangerLevel.equals("3")) {
                            listView.getChildAt(index).setBackgroundColor(Color.RED);
                            adapter.notifyDataSetChanged();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void checkIfAlertIsDangerous() {

    }




    public void addDataToList() {




        // call method to add child event
        // listener to get the child of our database
//        with orderByChild we sort all the alerts by danger level
        databaseReference.orderByChild("danger").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // TODO: if anything goes wrong let the Alert alert to only alert
                Alert alert = snapshot.getValue(Alert.class); // declared at the beginning of the code

                listView = findViewById(R.id.listView);
                list.add(alert.getTitle());
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method is called when the new child is added.
                // when the new child is added to our list we will be
                // notifying our adapter that data has changed.
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                // below method is called when we remove a child from our database.
                // inside this method we are removing the child from our array list
                // by comparing with it's value.
                // after removing the data we are notifying our adapter that the
                // data has been changed.

                //list.remove(snapshot.getValue(String.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                // this method is called when we move our
                // child in our database.
                // in our code we are note moving any child.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });












    }

    public void showAndManageAlert(String title,
                                   String category,
                                   String description,
                                   String location,
                                   String timestamp,
                                   String userId,
                                   String image,
                                   double lat,
                                   double lng,
                                   int dangerLevel,
                                   boolean isVerified,
                                   DataSnapshot dataSnapshot,
                                   String selectedListItem) {
        imgView = new ImageView(this);
        try {
            Picasso.get().load(Uri.parse(image)).resize(500,500).into(imgView);

        }
        catch(Exception e) {

        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Category: " + category +
                        "\nDescription: " + description +
                        "\nLocation: " + location +
                        "\nTimestamp: " + timestamp +
                        "\nUID: " + userId +
                        "\nLatitude: " + lat +
                        "\nLongtitude: " + lng +
                        "\nDanger Level: " + dangerLevel +
                        "\nIs verified: " + isVerified +
                        "\nImage: ")
                .setCancelable(true)
                .setView(imgView)
                .setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dataSnapshot.child(selectedListItem).getRef().removeValue();
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        addDataToList();
                    }
                })
                .setNeutralButton("Mark As Important", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Alert alert = dataSnapshot.child(selectedListItem).getValue(Alert.class);
                        toolbar.setTitle(alert.getTitle());
//                        HashMap hashMap = new HashMap<String, Alert>();
//
//                        hashMap.put("verified", true);
//                        dataSnapshot.child(selectedListItem).getRef().updateChildren(hashMap);
//                        alert.setVerified(true);
//                        dataSnapshot.child(selectedListItem).getRef().child("verified").setValue(alert.isVerified() );

                        alert.setVerified(true);
                        databaseReference.child(selectedListItem).child("verified").setValue(alert.isVerified());

                    }
                })

                .show();
    }

//    public void showMessage(String title, String message) {
//        new android.app.AlertDialog.Builder(this)
//                .setTitle(title)
//                .setMessage(message)
//                .setCancelable(true)
//                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                    }
//                })
//                .show();
//    }


    @Override
    public void onBackPressed() {
        // Here you want to show the user a dialog box
        new AlertDialog.Builder(this)
                .setTitle(R.string.signingOut)
                .setMessage(R.string.areYouSure)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user wants to leave - so dismiss the dialog and exit
                        dialog.dismiss();
                        mAuth.signOut();
                        Toast.makeText(AdminActivity.this, "You signed out from AdminActivity", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                        finish();

                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // The user is not sure, so you can exit or just stay
                        dialog.dismiss();
                    }
                }).show();

    }

    // FOR OPTION MENU TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.adminmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id){
            case R.id.orderingOptions:
                if(item.getTitle().toString().equals("Most dangerous alerts")) {
                    toolbar.setTitle("Most Dangerous Alerts");
                    item.setTitle("Less dangerous alerts");
                }
                else {
                    toolbar.setTitle("Less Dangerous Alerts");
                    item.setTitle("Most dangerous alerts");
                }

                Collections.reverse(list);
                adapter.notifyDataSetChanged();
                return true;

            case R.id.signOutOptions:
                mAuth.signOut();
                Toast.makeText(AdminActivity.this, "You signed out from UserActivity", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


























