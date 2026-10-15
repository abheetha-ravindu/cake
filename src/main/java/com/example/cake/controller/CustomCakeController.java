package com.example.cake.controller;

import com.example.cake.model.CakeOrder;
import com.example.cake.model.CustomCakeBooking;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/cakes")
public class CustomCakeController {

    private final ObjectMapper objectMapper;
    private final String textFilePath;
    private final String imageDir;
// Constructor for CustomCakeController, initializes the object mapper and file paths
    public CustomCakeController(
            ObjectMapper objectMapper,
            @Value("${cake.text-file-path:C:/cake_app/cakes.txt}") String textFilePath,
            @Value("${cake.image-dir:C:/cake_app/images/}") String imageDir) {
        this.objectMapper = objectMapper;
        this.textFilePath = textFilePath;
        this.imageDir = imageDir;
    }
// Get all cake orders from the text file and return them as a list of CakeOrder objects
    @GetMapping
    public ResponseEntity<List<CakeOrder>> getAllCakes() throws IOException {
        List<CakeOrder> cakeList = new ArrayList<>();
        Path dataFile = Path.of(textFilePath).toAbsolutePath().normalize();
        if (!Files.exists(dataFile)) {
            return ResponseEntity.ok(cakeList);
        }

        for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            if (line.startsWith("{")) {
                cakeList.add(objectMapper.readValue(line, CustomCakeBooking.class).toCakeOrder());
            } else {
                String[] data = line.split(",", 5);
                if (data.length == 5) {
                    cakeList.add(new CakeOrder(
                            data[0].trim(),
                            data[1].trim(),
                            data[2].trim(),
                            data[3].trim(),
                            data[4].trim()));
                }
            }
        }

        return ResponseEntity.ok(cakeList);
    }
// Create a new cake order from the provided form data and optional reference image, validate it, and save it to the text file
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CakeOrder> createBooking(
            @RequestParam Map<String, String> form,
            @RequestParam(value = "refImageFile", required = false) MultipartFile referenceImage)
            throws IOException {
        CustomCakeBooking booking = new CustomCakeBooking(
                required(form, "customerName"),
                required(form, "flavor"),
                required(form, "cakeSize"),
                form.getOrDefault("cakeMessage", "").trim(),
                required(form, "shape"),
                form.getOrDefault("filling", "").trim(),
                form.getOrDefault("decoration", "").trim(),
                form.getOrDefault("servings", "").trim(),
                required(form, "occasion"),
                required(form, "requiredDate"),
                form.getOrDefault("preferredTime", "").trim(),
                required(form, "contactNumber"),
                required(form, "email"),
                required(form, "pickupOrDelivery"),
                form.getOrDefault("deliveryAddress", "").trim(),
                form.getOrDefault("notes", "").trim(),
                form.getOrDefault("estimatedPrice", "").trim(),
                "");

        validateBooking(booking);
        booking = booking.withImagePath(saveReferenceImage(referenceImage, form.get("referenceImageUrl")));

        Path dataFile = Path.of(textFilePath).toAbsolutePath().normalize();
        Path parent = dataFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(
                dataFile,
                objectMapper.writeValueAsString(booking) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

        return ResponseEntity.status(HttpStatus.CREATED).body(booking.toCakeOrder());
    }
// Save the reference image to the specified directory and return the relative path, or validate the provided reference image URL
    private String saveReferenceImage(MultipartFile image, String referenceImageUrl) throws IOException {
        if (image != null && !image.isEmpty()) {
            if (image.getSize() > 5 * 1024 * 1024) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference image must be 5 MB or smaller.");
            }

            String extension = switch (image.getContentType() == null ? "" : image.getContentType().toLowerCase()) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Reference image must be a PNG, JPG, or WEBP file.");
            };

            Path imageDirectory = Path.of(imageDir).toAbsolutePath().normalize();
            Files.createDirectories(imageDirectory);
            String fileName = UUID.randomUUID() + extension;
            try (var inputStream = image.getInputStream()) {
                Files.copy(inputStream, imageDirectory.resolve(fileName));
            }
            return "/images/" + fileName;
        }

        if (referenceImageUrl == null || referenceImageUrl.isBlank()) {
            return "";
        }

        Path referencePath = Path.of(referenceImageUrl).normalize();
        if (referencePath.isAbsolute() || referencePath.startsWith("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reference image path.");
        }
        String encodedPath = UriUtils.encodePath(referencePath.toString().replace('\\', '/'), StandardCharsets.UTF_8);
        return "/images/" + encodedPath;
    }
// Get a required parameter from the form data, throwing an exception if it's missing or blank
    private String required(Map<String, String> form, String name) {
        String value = form.get(name);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required.");
        }
        return value.trim();
    }
//  Validate the provided CustomCakeBooking object, checking for valid shapes, sizes, pickup/delivery options, email format, and required date
    private void validateBooking(CustomCakeBooking booking) {
        if (!List.of("Round", "Square", "Heart", "Tiered").contains(booking.shape())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cake shape.");
        }
        if (!List.of("Small", "Medium", "Large", "Custom Servings").contains(booking.cakeSize())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cake size.");
        }
        if (!List.of("Pickup", "Delivery").contains(booking.pickupOrDelivery())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pickup or delivery option.");
        }
        if (!booking.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid email address.");
        }
        if (booking.pickupOrDelivery().equals("Delivery") && booking.deliveryAddress().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery address is required.");
        }
        try {
            if (LocalDate.parse(booking.requiredDate()).isBefore(LocalDate.now().plusDays(1))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required date must be at least one day ahead.");
            }
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required date must be a valid date.");
        }
    }
}
