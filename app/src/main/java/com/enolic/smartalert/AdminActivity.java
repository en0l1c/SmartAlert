package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.View;
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
import java.util.Date;
import java.util.HashMap;
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

    int listViewSelectedItem;

    Alert alert;

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


        //compareAlerts();


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);


        listView.setAdapter(adapter);





        // get the reference from firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference("ALERT");

        addDataToList();


        /////////////////////////////////////////////////
        // MAKE ALL DANGER 3 ITEMS RED
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int index = 0;

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String dangerLevel = dataSnapshot.child("danger").getValue(String.class);
                    Toast.makeText(AdminActivity.this, "danger level: " + dangerLevel, Toast.LENGTH_SHORT).show();

                    Toast.makeText(AdminActivity.this, listView.getChildAt(index).toString(), Toast.LENGTH_SHORT).show();

                if(dangerLevel.equals("3")) {
                    //listView.getChildAt(index).setBackgroundColor(Color.RED);
                }

                    index++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ////////////////////////////////////////////////



        //  ON CLICK LISTENER FOR LISTVIEW ITEM
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String selectedFromList = (String) listView.getItemAtPosition(position);

                databaseReference.orderByChild(selectedFromList).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Alert alert = snapshot.child(selectedFromList).getValue(Alert.class);

                        String dangerLevel = snapshot.child(selectedFromList).child("danger").getValue(String.class);


                        showAndManageAlert(alert.getTitle(),
                                alert.getCategory(),
                                alert.getDescription(),
                                alert.getLocation(),
                                alert.getTimestamp(),
                                alert.getUserId(),
                                alert.getImage(),
                                alert.getLat(),
                                alert.getLng(),
                                dangerLevel,
                                snapshot,
                                selectedFromList);


                        Toast.makeText(AdminActivity.this, "Position at ListView: " + String.valueOf(position), Toast.LENGTH_SHORT).show();


                        // print nearbyLocation titles for testing
//                        for(int i = 0; i <= nearbyLocations.size() - 1; i++) {
//                            Toast.makeText(AdminActivity.this, "NearbyLocation: " + nearbyLocations.get(i), Toast.LENGTH_SHORT).show();
//                        }




                        // change of list items that danger is 3
                        //changeListItemColor();
//                        for(int i = 0; i < dangerPos.size() - 1; i++) {
//                            Toast.makeText(AdminActivity.this, "DangerPos: " + dangerPos.get(i), Toast.LENGTH_SHORT).show();
//                        }

                        for(int i = 0; i <= nearbyLocations.size() - 1; i++) {
                            Toast.makeText(AdminActivity.this, "NearbyLocations: " + nearbyLocations.get(i) + "\nListSize: " + nearbyLocations.size(), Toast.LENGTH_SHORT).show();
                        }
//                        for(int i = nearbyLocations.size(); i >= 0; i--) {
//                            Toast.makeText(AdminActivity.this, "nls: " + i, Toast.LENGTH_SHORT).show();
//                        }
                        Toast.makeText(AdminActivity.this, "nls size: " + nearbyLocations.size(), Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




            }

        });

    }
    private void changeListItemColor() {
        String test = "";
        try {
            for(int i = 0; i <= dangerPos.size() - 1; i++) {
                listView.getChildAt(dangerPos.get(i)).setBackgroundColor(Color.RED);
                Toast.makeText(this, "DangerPos: " + dangerPos.get(i), Toast.LENGTH_SHORT).show();

                test += dangerPos.get(i) + " ";

            }
            Toast.makeText(this, dangerPos.get(0) + " List size: " + Math.abs(dangerPos.get(0) - dangerPos.size()), Toast.LENGTH_SHORT).show();
            Toast.makeText(this, test, Toast.LENGTH_SHORT).show();
        }
        catch(Exception e) {
            Toast.makeText(AdminActivity.this, "there is no danger 3 alerts", Toast.LENGTH_SHORT).show();
        }


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



    private void compareAlerts() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("ALERT");
        //listView = findViewById(R.id.listView);


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                dataRef.addValueEventListener(new ValueEventListener() {
                    String categoryLast;
                    String categoryCurrent;

                    String timestampLast;
                    String timestampCurrent;

                    double latCurrent = 0;
                    double lngCurrent = 0;
                    double latLast;
                    double lngLast;
                    int userCounter = 0;
                    double distance;
                    String titleLast;


                    HashMap hashMap = new HashMap<String, String>();
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // edw tha parei to location apo to antistoixo item tis realtime database kai meta stin for parakatw tha sugkrinetai to kathe location me autoj




                        // get the first location and with for check all other locations if nearby
                        try {
                            latLast = alert.getLat();
                            lngLast = alert.getLng();
                            titleLast = alert.getTitle();

                            timestampLast = alert.getTimestamp();
                            categoryLast = alert.getCategory();
                        }
                        catch(Exception e) {

                        }

//                Toast.makeText(AdminActivity.this, "latLast: " + latLast, Toast.LENGTH_SHORT).show();


                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            //Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);

                            // save last location and current location to compare them on isLocationValid method


                            try {


                                latCurrent = alert.getLat();
                                lngCurrent = alert.getLng();

                                //boolean isValidAlert = true;

                                // save last timestamp and current timestamp to compare them on isValidTime method
                                //timestampLast = timestampCurrent; // time when alert submitted
                                timestampCurrent = alert.getTimestamp();

                                //timestampCurrent = String.valueOf(System.currentTimeMillis() / 1000); // current time


                                // categories
                                //categoryLast = categoryCurrent;
                                categoryCurrent = alert.getCategory();



                                distance = calcDistance(latLast, lngLast, latCurrent, lngCurrent);




//                    if(distance < 0.9 && Objects.equals(categoryLast, categoryCurrent) && isTimeValid(timestampLast, timestampCurrent))
                                if(distance < 0.9){
                                    nearbyLocations.add(alert.getTitle());
//                        if there is more than 6 alerts on the same location the dangerous level goes to 3
                                    if(userCounter >= 6) {
//                                        if(isTimeValid(timestampLast, timestampCurrent) && Objects.equals(categoryLast, categoryCurrent)) {
//                                            hashMap.put("danger", "3.");
//                                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                                        }
//                                        else {
//                                            hashMap.put("danger", "22");
//                                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                                        }

                                    }
                                    else {
                                        hashMap.put("danger", "2.");
                                        databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
                                    }
                                    userCounter++;

                                }
                                else {
                                    hashMap.put("danger", "1.");
                                    databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                        Toast.makeText(AdminActivity.this, alert.getTitle() + " false", Toast.LENGTH_SHORT).show();

                                }



                            }
                            catch(Exception e) {

                            }




                        }
                        userCounter = 0;
                        latCurrent = 0;
                        lngCurrent = 0;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


//        dataRef.addValueEventListener(new ValueEventListener() {
//            String categoryLast;
//            String categoryCurrent;
//
//            String timestampLast;
//            String timestampCurrent;
//
//            double latCurrent = 0;
//            double lngCurrent = 0;
//            double latLast;
//            double lngLast;
//            int userCounter = 0;
//            double distance;
//            String titleLast;
//
//
//            HashMap hashMap = new HashMap<String, String>();
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                // edw tha parei to location apo to antistoixo item tis realtime database kai meta stin for parakatw tha sugkrinetai to kathe location me autoj
//
//
//
//
//                // get the first location and with for check all other locations if nearby
//                try {
//                    latLast = alert.getLat();
//                    lngLast = alert.getLng();
//                    titleLast = alert.getTitle();
//                }
//                catch(Exception e) {
//
//                }
//
////                Toast.makeText(AdminActivity.this, "latLast: " + latLast, Toast.LENGTH_SHORT).show();
//
////                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
////
////                }
//
//
//                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
////                    Alert alert = snapshot.child(dataSnapshot.getKey()).getValue(Alert.class);
//
//                    // save last location and current location to compare them on isLocationValid method
//
//
//                    latCurrent = alert.getLat();
//                    lngCurrent = alert.getLng();
//
////                    Toast.makeText(AdminActivity.this, "latCurrent: " + latCurrent, Toast.LENGTH_SHORT).show();
//
//
//
//
//
//                    //boolean isValidAlert = true;
//
//                    // save last timestamp and current timestamp to compare them on isValidTime method
//                    timestampLast = timestampCurrent; // time when alert submitted
//                    timestampCurrent = alert.getTimestamp();
//                    //timestampCurrent = String.valueOf(System.currentTimeMillis() / 1000); // current time
//
//
//                    // categories
//                    categoryLast = categoryCurrent;
//                    categoryCurrent = alert.getCategory();
//
//
//
//                    distance = calcDistance(latLast, lngLast, latCurrent, lngCurrent);
//
//                    showMessage("", "First Location: \n" +
//                            "latCurrent: " + latLast + "," + lngLast +
//                            "\nLocationTitle: " + titleLast +
//                            "\ndisatnce: " + distance +
//                            "\n\nSecond Location: \n" +
//                            "latCurrent: " + latCurrent + "," + lngCurrent +
//                            "\nLocationTitle: " + alert.getTitle() +
//                            "\ndisatnce: " + distance);
//
//
//
////                    if(distance < 0.9 && Objects.equals(categoryLast, categoryCurrent) && isTimeValid(timestampLast, timestampCurrent))
//                    if(distance < 0.9 && isTimeValid(timestampLast, timestampCurrent)){
//                        nearbyLocations.add(alert.getTitle());
////                        if there is more than 6 alerts on the same location the dangerous level goes to 3
//                        if(userCounter >= 6) {
//                            hashMap.put("danger", "3");
//                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                        }
//                        else {
//                            hashMap.put("danger", "2");
//                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
//                        }
//                        userCounter++;
//
//                    }
//                    else {
//                        hashMap.put("danger", "1");
//                        databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
////                        Toast.makeText(AdminActivity.this, alert.getTitle() + " false", Toast.LENGTH_SHORT).show();
//
//                    }
//
//
//                    // 1st alert set danger to 1, 2nd-6th set danger to 2, and the 7th alert on the location border and in-time set danger to 3
////                    if((distance < 0.9) &&
////                            isTimeValid(timestampLast, timestampCurrent) &&
////                            Objects.equals(categoryLast, categoryCurrent)) {
////                        if(userCounter>=6) {
////                            hashMap.put("danger", "3");
////                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
////                        }
////                        else {
////                            hashMap.put("danger", "2");
////                            databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
////                        }
////                        userCounter++;
////
////                    }
////                    else {
////                        hashMap.put("danger", "1");
////                        databaseReference.child(dataSnapshot.getKey()).updateChildren(hashMap);
////                        //userCounter--;
////                    }
//
//                }
//                userCounter = 0;
//                latCurrent = 0;
//                lngCurrent = 0;
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


    }

    public void addDataToList() {



        // call method to add child event
        // listener to get the child of our database
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // TODO: if anything goes wrong let the Alert alert to only alert
                Alert alert = snapshot.getValue(Alert.class); // declared at the beginning of the code

                listView = findViewById(R.id.listView);
                list.add(alert.getTitle());
                adapter.notifyDataSetChanged();


                String dangerLevel = snapshot.child("danger").getValue(String.class);
                String ts;
//                if(dangerLevel.equals("3")) {
//                    //Toast.makeText(AdminActivity.this, "addDataToList: " + String.valueOf(list.size()), Toast.LENGTH_SHORT).show();
//
//                    ts = alert.getTimestamp();
//                    // to position tou list item pou thelw na allaksw background color tautizetai me to list size (index)
//                    // parola auta den leitourgei i allagi tou background color sto parakatw try
//                    dangerPos.add(list.size());
//                    dangerPosByTs.add(ts);
//                }
                //Toast.makeText(AdminActivity.this, "DangerPosSize: " + dangerPos.size(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(AdminActivity.this, "DangerPosByListSize: " + dangerPosByTs.size(), Toast.LENGTH_SHORT).show();

//                for(int i = 0; i < dangerPos.size() - 1; i++) {
//                    Toast.makeText(AdminActivity.this, "a DangerPos: " + dangerPos.get(i), Toast.LENGTH_SHORT).show();
//                }
//                try {
//
//                }
//                catch(Exception e) {
//                    Toast.makeText(AdminActivity.this, "aaaaaaa", Toast.LENGTH_SHORT).show();
//
//                }





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
                                   String dangerLevel,
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

                .show();
    }

    public void showMessage(String title, String message) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .show();
    }


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
}


























