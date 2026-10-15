package com.example.cake.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.ObjectMapper;

class CustomCakeControllerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void savesBookingAndUploadedImageAndReturnsItFromGet() throws Exception {
        Path dataFile = temporaryDirectory.resolve("cakes.txt");
        Path imageDirectory = temporaryDirectory.resolve("images");
        CustomCakeController controller = new CustomCakeController(
                new ObjectMapper(), dataFile.toString(), imageDirectory.toString());
        MockMultipartFile image = new MockMultipartFile(
                "refImageFile", "reference.png", "image/png", new byte[] { 1, 2, 3 });

        var response = controller.createBooking(validForm(), image);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        try (var images = Files.list(imageDirectory)) {
            assertEquals(1, images.count());
        }
        assertTrue(Files.readString(dataFile).contains("\"customerName\":\"Ava Baker\""));
        assertEquals("Ava Baker", controller.getAllCakes().getBody().get(0).getCustomerName());
        assertTrue(controller.getAllCakes().getBody().get(0).getImagePath().startsWith("/images/"));
    }

    @Test
    void readsExistingCommaSeparatedCakeOrders() throws Exception {
        Path dataFile = temporaryDirectory.resolve("cakes.txt");
        Files.writeString(dataFile, "Ava Baker,Vanilla,Medium,Happy Birthday,/images/cake.png\n");
        CustomCakeController controller = new CustomCakeController(
                new ObjectMapper(), dataFile.toString(), temporaryDirectory.resolve("images").toString());

        var orders = controller.getAllCakes().getBody();

        assertEquals(1, orders.size());
        assertEquals("Happy Birthday", orders.get(0).getTextOnCake());
    }

    @Test
    void rejectsInvalidBookingBeforeSavingFiles() {
        Path dataFile = temporaryDirectory.resolve("cakes.txt");
        CustomCakeController controller = new CustomCakeController(
                new ObjectMapper(), dataFile.toString(), temporaryDirectory.resolve("images").toString());
        Map<String, String> form = new java.util.HashMap<>(validForm());
        form.put("email", "not-an-email");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createBooking(form, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(Files.notExists(dataFile));
    }

    private Map<String, String> validForm() {
        return Map.ofEntries(
                Map.entry("customerName", "Ava Baker"),
                Map.entry("flavor", "Vanilla"),
                Map.entry("cakeSize", "Medium"),
                Map.entry("cakeMessage", "Happy Birthday"),
                Map.entry("shape", "Round"),
                Map.entry("filling", "Vanilla Cream"),
                Map.entry("decoration", "Floral Style"),
                Map.entry("occasion", "Birthday"),
                Map.entry("requiredDate", LocalDate.now().plusDays(2).toString()),
                Map.entry("preferredTime", "Afternoon"),
                Map.entry("contactNumber", "0771234567"),
                Map.entry("email", "ava@example.com"),
                Map.entry("pickupOrDelivery", "Pickup"),
                Map.entry("deliveryAddress", ""),
                Map.entry("notes", ""),
                Map.entry("estimatedPrice", "6800.00"),
                Map.entry("referenceImageUrl", "Customise cake/Round Vanila cake.png"));
    }
}
