package com.example.cake.Admin.Model;

public class Custome {

    private final String id;
    private final String name;
    private final String description;
    private final String imageUrl;

    private final String username;
    private final String email;
    private final String address;
    private final String phoneNumber;

    public Custome(String id, String name, String description, String imageUrl, String username, String email, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.username = username;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    
}
