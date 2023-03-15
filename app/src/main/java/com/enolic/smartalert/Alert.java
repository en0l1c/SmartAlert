package com.enolic.smartalert;

public class Alert {
    private String title;
    private String timestamp;
    private String location;
    private String description;
    private String category;
    private String image;
    private String userId;
    private int dangerLevel;
    private double lat;
    private double lng;

    public Alert() {

    }

    public Alert(String title,
                 String timestasmp,
                 String location,
                 String description,
                 String category,
                 String image,
                 String userId,
                 double lat,
                 double lng) {
        this.title = title;
        this.timestamp = timestasmp;
        this.location = location;
        this.description = description;
        this.category = category;
        this.image = image;
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public String getTimestamp() {
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
