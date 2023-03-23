package com.enolic.smartalert;

import java.util.Comparator;

public class Alert {
    private String title;
    private int timestamp;
    private String location;
    private String description;
    private String category;
    private String image;
    private String userId;
//    private int dangerLevel;
    private double lat;
    private double lng;
    private boolean verified;
    private int danger;

    public Alert() {

    }

    public Alert(String title,
                 int timestasmp,
                 String location,
                 String description,
                 String category,
                 String image,
                 String userId,
                 double lat,
                 double lng,
                 int danger,
                 boolean verified) {
        this.title = title;
        this.timestamp = timestasmp;
        this.location = location;
        this.description = description;
        this.category = category;
        this.image = image;
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
        this.danger = danger;
        this.verified = verified;
    }
    public int getDanger() {
        return danger;
    }

    public void setDanger(int danger) {
        this.danger = danger;
    }



    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getTitle() {
        return title;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getImage() {
        return image;
    }

    public String getUserId() {
        return userId;
    }



}









