package com.enolic.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.ConversationActions;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.storage.StorageReference;
import androidx.core.app.NotificationCompat;


import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    ListView listView;
    ArrayList<String> list = new ArrayList<>();
//    ArrayAdapter<String> adapter;

    StorageReference storageRef;
    ImageView imgView;
    ArrayList<Integer> dangerPos = new ArrayList<Integer>();
    ArrayList<String> dangerPosByTs = new ArrayList<String>();
    ArrayList<String> nearbyLocations = new ArrayList<String>();
    ArrayList<String> whichDanger3 = new ArrayList<>();
    private List<DataSnapshot> alerts = new ArrayList<>();
    ArrayAdapter<DataSnapshot> adapter;


    int listViewSelectedItem;

    Alert alert;
    Toolbar toolbar;

    //String timestampLast = "0";
    //String timestampCurrent;
    final static String SERVER_KEY = "AAAABW3eEKo:APA91bFvt_1oQKD6L3zNTiD6t6S-w-qqCwouOy_6g-3Vnrq53QE9ifvWMADbXuQOLiuHi6AQIEF1KD4ObL7sAQTjYkGfhbi_uETnhiDtlq7WUsply4b_aiSLtS53BQR0tCxtZ1B3zNET";

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = SERVER_KEY;
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        listView = findViewById(R.id.listView);

        mAuth = LoginActivity.mAuth;
        //database = FirebaseDatabase.getInstance();
        //databaseReference = database.getReference("ALERTS");


        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Less dangerous alerts");
        setSupportActionBar(toolbar);



        // Initialize Firebase
//        FirebaseApp.initializeApp(AdminActivity.this);
        database = FirebaseDatabase.getInstance();




//        DatabaseReference alertsRef = database.getReference("ALERT");
//        DatabaseReference usersRef = database.getReference("USER");
//
//        // Listen for new alerts
//        alertsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                // Check if alert is verified
//                if (dataSnapshot.child("verified").getValue(Boolean.class)) {
//                    String alertId = dataSnapshot.getKey();
//
//                    // Find nearby users and send notifications
//                    findNearbyUsers(alertId);
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//
//            // Other ChildEventListener methods
//            // ...
//        });

        // Set up notification channels
//        createNotificationChannels();

//        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
//
//
//
//        listView.setAdapter(adapter);











        // get the reference from firebase database
        databaseReference = FirebaseDatabase.getInstance().getReference("ALERT");

        addDataToList();


        //  ON CLICK LISTENER FOR LISTVIEW ITEM
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                DataSnapshot alertSnapshot = (DataSnapshot) adapterView.getItemAtPosition(position);

                        showAndManageAlert(alertSnapshot.child("title").getValue(String.class),
                                alertSnapshot.child("category").getValue(String.class),
                                alertSnapshot.child("description").getValue(String.class),
                                alertSnapshot.child("location").getValue(String.class),
                                alertSnapshot.child("timestamp").getValue(Integer.class),
                                alertSnapshot.child("userId").getValue(String.class),
                                alertSnapshot.child("image").getValue(String.class),
                                alertSnapshot.child("lat").getValue(Integer.class),
                                alertSnapshot.child("lng").getValue(Integer.class),
                                alertSnapshot.child("danger").getValue(Integer.class),
                                alertSnapshot.child("verified").getValue(Boolean.class),
                                alertSnapshot,
                                view,
                                position);


//                final String selectedFromList = (String) listView.getItemAtPosition(position);

//                databaseReference.orderByChild(selectedFromList).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
////                        Alert alert = snapshot.child(selectedFromList).getValue(Alert.class);
//                        Alert alert = (Alert) adapterView.getItemAtPosition(position);
//
////                        String dangerLevel = snapshot.child(selectedFromList).child("danger").getValue(Integer.class);
//
//
//                        showAndManageAlert(alert.getTitle(),
//                                alert.getCategory(),
//                                alert.getDescription(),
//                                alert.getLocation(),
//                                alert.getTimestamp(),
//                                alert.getUserId(),
//                                alert.getImage(),
//                                alert.getLat(),
//                                alert.getLng(),
//                                alert.getDanger(),
//                                alert.isVerified(),
//                                snapshot,
//                                selectedFromList,
//                                position);
//
//
//                        Toast.makeText(AdminActivity.this, "Position at ListView: " + String.valueOf(position), Toast.LENGTH_SHORT).show();
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });




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






    public void addDataToList() {





// Define a class variable to hold the list of alerts


// ...

// Retrieve the data from the database and sort the list of alerts
        DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference().child("ALERT");
        alertRef.orderByChild("danger").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                alerts.clear();
                for (DataSnapshot alertSnapshot : snapshot.getChildren()) {
                    Alert alert = snapshot.child(alertSnapshot.getKey()).getValue(Alert.class);

                    boolean verified = alert.isVerified();
                    int dangerLevel = alert.getDanger();
                    long timestamp = alert.getTimestamp();
//                    if (verified) {
//                        // Add the verified alert to the front of the list
//                        alerts.add(0, alertSnapshot);
//                    } else
                    if (dangerLevel == 3) {
                    // Add the alert to the list if it has danger level 3
                        int index = alerts.size();
                        for (int i = 0; i < alerts.size(); i++) {
                            int danger = alerts.get(i).child("danger").getValue(Integer.class);
                            long otherTimestamp = alerts.get(i).child("timestamp").getValue(Long.class);
                            if (dangerLevel < danger || (danger == dangerLevel && timestamp > otherTimestamp)) {
                                index = i;
                                break;
                            }
                        }
                        alerts.add(index, alertSnapshot);
                    } else if (dangerLevel == 2) {
                        // Add the alert to the list if it has danger level 2
                        int index = alerts.size();
                        for (int i = 0; i < alerts.size(); i++) {
                            int danger = alerts.get(i).child("danger").getValue(Integer.class);
                            long otherTimestamp = alerts.get(i).child("timestamp").getValue(Long.class);
                            if (dangerLevel < danger || (danger == dangerLevel && timestamp > otherTimestamp)) {
                                index = i;
                                break;
                            }
                        }
                        alerts.add(index, alertSnapshot);
                    } else {
                        // Add the alert to the list if it has danger level 1
                        int index = alerts.size();
                        for (int i = 0; i < alerts.size(); i++) {
                            int danger = alerts.get(i).child("danger").getValue(Integer.class);
                            long otherTimestamp = alerts.get(i).child("timestamp").getValue(Long.class);
                            if (dangerLevel < danger || (danger == dangerLevel && timestamp > otherTimestamp)) {
                                index = i;
                                break;
                            }
                        }
                        alerts.add(index, alertSnapshot);
                    }
                }
                Collections.reverse(alerts); // Reverse the list to sort by descending timestamp
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error here
            }
        });


        // Set the adapter for the ListView
        adapter = new ArrayAdapter<DataSnapshot>(this, android.R.layout.simple_list_item_1, android.R.id.text1, alerts) {

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                TextView titleTextView = convertView.findViewById(android.R.id.text1);
                DataSnapshot snapshot = getItem(position);
                boolean verified = snapshot.child("verified").getValue(Boolean.class);
                int danger = snapshot.child("danger").getValue(Integer.class);
                String title = snapshot.child("title").getValue(String.class);
                if (verified ) {
                    title = "* " + title; // Add a "*" to the beginning of the title for verified and danger=3 alerts
                    convertView.setBackgroundColor(Color.RED); // Set the background color to red for verified alerts
                }
                else if(danger == 3)  {
                    convertView.setBackgroundColor(Color.YELLOW);
                    titleTextView.setTextColor(Color.BLACK); // check to set the right color for dark or light theme
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT); // Set the background color to transparent for non-verified alerts
                    titleTextView.setTextColor(Color.WHITE); // check to set the right color for dark or light theme

                }
                titleTextView.setText(title);
                return convertView;
            }
        };

        listView.setAdapter(adapter);








//        databaseReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // TODO: if anything goes wrong let the Alert alert to only alert
//                Alert alert = snapshot.getValue(Alert.class); // declared at the beginning of the code
//
//                listView = findViewById(R.id.listView);
//                list.add(alert.getTitle());
//                adapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                // this method is called when the new child is added.
//                // when the new child is added to our list we will be
//                // notifying our adapter that data has changed.
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                // below method is called when we remove a child from our database.
//                // inside this method we are removing the child from our array list
//                // by comparing with it's value.
//                // after removing the data we are notifying our adapter that the
//                // data has been changed.
//
//                //list.remove(snapshot.getValue(String.class));
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                // this method is called when we move our
//                // child in our database.
//                // in our code we are note moving any child.
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });












    }

    public void showAndManageAlert(String title,
                                   String category,
                                   String description,
                                   String location,
                                   int timestamp,
                                   String userId,
                                   String image,
                                   double lat,
                                   double lng,
                                   int dangerLevel,
                                   boolean isVerified,
                                   DataSnapshot alertSnapshot,
                                   View selectedView,
                                   int position) {
        imgView = new ImageView(this);
        try {
            Picasso.get().load(Uri.parse(image)).resize(500,500).into(imgView);

        }
        catch(Exception e) {

        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        // CONVERT UNIX TIMESTAMP TO REAL TIME
        // convert timestamp from seconds to millis
        int timestampSeconds = timestamp* 1000;
        Date date = new Date(timestampSeconds);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss yyyy-MM-dd z");
        sdf.setTimeZone(TimeZone.getDefault());
        String formattedDate = sdf.format(date);

        // check if image is null and write it to dialog message
        String isImageNull = "";
        if(image.equals("")) {
            isImageNull = "null";
        }

//        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title)
        .setMessage("Time: " + formattedDate +
                "\nCategory: " + category +
                "\nDescription: " + description +
                "\nLocation: " + location +
                "\nTimestamp: " + timestamp +
                "\nUID: " + userId +
                "\nDanger Level: " + dangerLevel +
                "\nIs verified: " + isVerified +
                "\nImage: " + isImageNull)
        .setCancelable(true)
        .setView(imgView)
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
                alertSnapshot.getRef().removeValue();
                adapter.clear();
                adapter.notifyDataSetChanged();
                addDataToList();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button neutralButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        String neutralBtnText = "Mark As important";
        if(alertSnapshot.child("verified").getValue(Boolean.class)) {
            // NORMAL
            neutralBtnText = "Mark as normal";
            neutralButton.setText(neutralBtnText);

            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Define the alert
                    Alert alert = alertSnapshot.getValue(Alert.class);

                    // Set verified of the alert to false
                    alert.setVerified(false);
                    // Set the danger Level to 1
                    alert.setDanger(1);
                    // Save the alert
                    alertSnapshot.getRef().setValue(alert);

//                    alertSnapshot.getRef().child("verified").setValue(false);

                    selectedView.setBackgroundColor(Color.TRANSPARENT);
                    dialog.dismiss();

                }
            });

        }
        else {
            // IMPORTANT
            neutralBtnText = "Mark as important";
            neutralButton.setText(neutralBtnText);

            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Define the alert
                    Alert alert = alertSnapshot.getValue(Alert.class);

                    // Set verified to true
                    alert.setVerified(true);
                    // Set danger level to 3
                    alert.setDanger(3);

                    //Save the alert
                    alertSnapshot.getRef().setValue(alert);

                    // alertSnapshot.getRef().child("verified").setValue(true); <-- instead of using Alert.class

                    selectedView.setBackgroundColor(Color.RED);
                    dialog.dismiss();



                    findNearbyUsers(alert);
                }
            });
        }
    }



    private void findNearbyUsers(Alert alert) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("USER");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> nearbyFcmTokens = new HashSet<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    double userLat = userSnapshot.child("lat").getValue(Double.class);
                    double userLng = userSnapshot.child("lng").getValue(Double.class);
                    String userToken = userSnapshot.child("fcmToken").getValue(String.class);
                    // Calculate the distance between the user and the alert
                    float[] results = new float[1];
                    Location.distanceBetween(alert.getLat(), alert.getLng(), userLat, userLng, results);

                    // Check if the user is nearby (within 1 kilometer)
                    if (results[0] <= 1000) {
                        nearbyFcmTokens.add(userToken);
                    }
                }

                // Send push notifications to all nearby users
                sendNotificationToUsers(nearbyFcmTokens, alert.getTitle(), alert.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LoG", "Failed to read value.", error.toException());
            }
        });
    }

    private void sendNotificationToUsers(Set<String> tokens, String title, String message) {
        // Create the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build the notification message
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create the intent to open the app when the user taps on the notification
        Intent intent = new Intent(this, AdminActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // Send the notification to each nearby user on a background thread
        new Thread(() -> {
            for (String token : tokens) {
                if (token == null || token.isEmpty()) {
                    return;
                }

                Log.d("FCM", "Sending notification to FCM token(sendMessage): " + token);
                Log.d("FCM", "Notification title: " + title);
                Log.d("FCM", "Notification message: " + message);

                // Create the payload data for the Firebase Cloud Function
                Map<String, Object> payload = new HashMap<>();
                payload.put("title", title);
                payload.put("message", message);
                payload.put("token", token);



                // Call the Firebase Cloud Function to send the push notification
                FirebaseFunctions.getInstance()
                        .getHttpsCallable("sendNotificationToUser")
                        .call(payload)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Object result = task.getResult().getData();
                                if (result instanceof String) {
                                    Log.d("FCM", "Response string: " + result.toString());
                                } else if (result instanceof JSONObject) {
                                    try {
                                        JSONObject json = new JSONObject(result.toString());
                                        if (json.has("success") && json.getBoolean("success")) {
                                            Log.d("FCM", "Notification sent successfully to FCM token: " + token);
                                        } else {
                                            Log.e("FCM", "Error sending notification to FCM token: " + token + ", message: " + json.optString("message"));
                                        }
                                    } catch (JSONException e) {
                                        Log.e("FCM", "Error parsing JSON response: " + result.toString(), e);
                                    }
                                }
                            } else {
                                Log.e("FCM", "Error sending notification to FCM token: " + token, task.getException());
                            }
                        });
            }
        }).start();
    }



    private void getUserFcmToken(String uid, Consumer<String> callback) {
        DatabaseReference usersRef = database.getReference("USER");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fcmToken = dataSnapshot.child("fcmToken").getValue(String.class);
                Log.d("MyApp", "FCM Token for " + uid + " is " + fcmToken);

                callback.accept(fcmToken);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
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


























