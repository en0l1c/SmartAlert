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

    public User() {

    }
    public User(String email, String name, double lat, double lng) {
        this.email = email;
        this.name = name;
        this.lat = lat;
        this.lng = lng;

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
