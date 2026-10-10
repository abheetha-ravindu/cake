package com.example.cake.model;

public record CustomCakeBooking(
        String customerName,
        String cakeFlavor,
        String cakeSize,
        String cakeMessage,
        String shape,
        String filling,
        String decoration,
        String servings,
        String occasion,
        String requiredDate,
        String preferredTime,
        String contactNumber,
        String email,
        String pickupOrDelivery,
        String deliveryAddress,
        String notes,
        String estimatedPrice,
        String imagePath) {

    public CakeOrder toCakeOrder() {
        return new CakeOrder(customerName, cakeFlavor, cakeSize, cakeMessage, imagePath);
    }

    public CustomCakeBooking withImagePath(String imagePath) {
        return new CustomCakeBooking(
                customerName,
                cakeFlavor,
                cakeSize,
                cakeMessage,
                shape,
                filling,
                decoration,
                servings,
                occasion,
                requiredDate,
                preferredTime,
                contactNumber,
                email,
                pickupOrDelivery,
                deliveryAddress,
                notes,
                estimatedPrice,
                imagePath);
    }
}
