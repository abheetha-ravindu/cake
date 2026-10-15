package com.example.cake.Admin.Model;

import java.util.ArrayList;
import java.util.List;

public class Custome {

    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private String username;
    private String email;
    private String address;
    private String phoneNumber;
    private String orderDate;
    private String occasion;
    private String cakeShape;
    private String cakeFlavor;
    private String cakeSize;
    private String filling;
    private String decoration;
    private List<String> toppings = new ArrayList<>();
    private String cakeMessage;
    private String requiredDate;
    private String fulfillmentMethod;
    private String preferredTime;
    private String specialRequests;
    private String comments;
    private String estimatedTotal;

    public Custome() {
    }

    public Custome(String id, String name, String description, String imageUrl, String username,
            String email, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.username = username;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.specialRequests = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public String getOccasion() { return occasion; }
    public void setOccasion(String occasion) { this.occasion = occasion; }
    public String getCakeShape() { return cakeShape; }
    public void setCakeShape(String cakeShape) { this.cakeShape = cakeShape; }
    public String getCakeFlavor() { return cakeFlavor; }
    public void setCakeFlavor(String cakeFlavor) { this.cakeFlavor = cakeFlavor; }
    public String getCakeSize() { return cakeSize; }
    public void setCakeSize(String cakeSize) { this.cakeSize = cakeSize; }
    public String getFilling() { return filling; }
    public void setFilling(String filling) { this.filling = filling; }
    public String getDecoration() { return decoration; }
    public void setDecoration(String decoration) { this.decoration = decoration; }
    public List<String> getToppings() { return toppings; }
    public void setToppings(List<String> toppings) {
        this.toppings = toppings == null ? new ArrayList<>() : new ArrayList<>(toppings);
    }
    public String getCakeMessage() { return cakeMessage; }
    public void setCakeMessage(String cakeMessage) { this.cakeMessage = cakeMessage; }
    public String getRequiredDate() { return requiredDate; }
    public void setRequiredDate(String requiredDate) { this.requiredDate = requiredDate; }
    public String getFulfillmentMethod() { return fulfillmentMethod; }
    public void setFulfillmentMethod(String fulfillmentMethod) { this.fulfillmentMethod = fulfillmentMethod; }
    public String getPreferredTime() { return preferredTime; }
    public void setPreferredTime(String preferredTime) { this.preferredTime = preferredTime; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public String getEstimatedTotal() { return estimatedTotal; }
    public void setEstimatedTotal(String estimatedTotal) { this.estimatedTotal = estimatedTotal; }
}
