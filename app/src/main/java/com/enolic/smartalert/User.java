package com.enolic.smartalert;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String email;
    private String name;
    private String role;

    public User() {

    }
    public User(String email, String name) {
        this.email = email;
        this.name = name;

        if(email.equals("p18023@unipi.gr"))
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
}
