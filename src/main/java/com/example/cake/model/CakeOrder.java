package com.example.cake.model;

public class CakeOrder {
    private String customerName;
    private String cakeFlavor;
    private String cakeSize;
    private String textOnCake;
    private String imagePath;

    public CakeOrder(String customerName, String cakeFlavor, String cakeSize, String textOnCake, String imagePath) {
        this.customerName = customerName;
        this.cakeFlavor = cakeFlavor;
        this.cakeSize = cakeSize;
        this.textOnCake = textOnCake;
        this.imagePath = imagePath;
    }
    public String getCustomerName() { return customerName; }
    public String getCakeFlavor() { return cakeFlavor; }
    public String getCakeSize() { return cakeSize; }
    public String getTextOnCake() { return textOnCake; }
    public String getImagePath() { return imagePath; }
    
}
