package com.example.cake.Admin.Model;

public class Item {
    private final String id;
    private final String name;
    private final String description;
    private final String flavor;
    private final String size;
    private final double price;
    private final int shape;
    private final String category;
    private final String availability;
    private final String imageUrl;

    public Item(String id, String name, String description, String flavor, String size, double price, int shape, String category, String availability, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.flavor = flavor;
        this.size = size;
        this.price = price;
        this.shape = shape;
        this.category = category;
        this.availability = availability;

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

    public String getFlavor() {
        return flavor;
    }

    public String getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public int getShape() {
        return shape;
    }

    public String getCategory() {
        return category;
    }

    public String getAvailability() {
        return availability;
    }
}
