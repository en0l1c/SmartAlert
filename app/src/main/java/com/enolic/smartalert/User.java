package com.enolic.smartalert;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String email;
    private String name;
    private String role;
    private double lat;
    private double lng;
    private String fcmToken;

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public User() {

    }
    public User(String email, String name, double lat, double lng, String fcmToken) {
        this.email = email;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.fcmToken = fcmToken;

        if(email.equals("p18023@unipi.gr") || email.equals("stefbou9@gmail.com"))
            this.role = "admin";
        else
            this.role = "user";
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
