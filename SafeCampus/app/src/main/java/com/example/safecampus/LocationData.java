package com.example.safecampus;

public class LocationData {

    private String id;
    private String type;
    private String description;
    private double latitude;
    private double longitude;
    private String timestamp;
    private String username; // baru

    public LocationData() {} // Required for Firestore

    public LocationData(String id, String type, String description, double latitude,
                        double longitude, String timestamp, String username) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.username = username;
    }

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimestamp() { return timestamp; }
    public String getUsername() { return username; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setUsername(String username) { this.username = username; }
}