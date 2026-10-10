package com.example.cake.Admin.Services;

import com.example.cake.Admin.Model.Item;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ItemService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final String IMAGE_URL_PREFIX = "/item-images/";

    private final Path dataDirectory;
    private final Path imageDirectory;
    private final Path databaseFile;

    public ItemService(@Value("${cake.item-data-dir:src/main/resources/static/item}") String itemDataDirectory) {
        this.dataDirectory = Path.of(itemDataDirectory).toAbsolutePath().normalize();
        this.imageDirectory = dataDirectory.resolve("images");
        this.databaseFile = dataDirectory.resolve("item.txt");
    }

    public List<Item> findAllItems() throws IOException {
        if (!Files.exists(databaseFile)) {
            return List.of();
        }

        List<Item> items = new ArrayList<>();
        for (String line : Files.readAllLines(databaseFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(";", -1);
            if (fields.length != 4 && fields.length != 10) {
                throw new IOException("Invalid item record in " + databaseFile);
            }
            if (fields.length == 4) {
                items.add(new Item(
                        decode(fields[0]),
                        decode(fields[1]),
                        decode(fields[2]),
                        "",
                        "",
                        0.0,
                        0,
                        "Cake",
                        "Available",
                        IMAGE_URL_PREFIX + decode(fields[3])));
            } else {
                try {
                    items.add(new Item(
                            decode(fields[0]),
                            decode(fields[1]),
                            decode(fields[2]),
                            decode(fields[3]),
                            decode(fields[4]),
                            Double.parseDouble(decode(fields[5])),
                            Integer.parseInt(decode(fields[6])),
                            decode(fields[7]),
                            decode(fields[8]),
                            IMAGE_URL_PREFIX + decode(fields[9])));
                } catch (NumberFormatException exception) {
                    throw new IOException("Invalid numeric item field in " + databaseFile, exception);
                }
            }
        }
        return items;
    }

    public void saveItem(
            String name,
            String description,
            String flavor,
            String size,
            double price,
            int shape,
            String category,
            String availability,
            MultipartFile imageFile) throws IOException {
        if (name == null || name.isBlank() || description == null || description.isBlank()) {
            throw new IllegalArgumentException("Name and description are required.");
        }
        if (flavor == null || flavor.isBlank() || size == null || size.isBlank()
                || category == null || category.isBlank()
                || availability == null || availability.isBlank()) {
            throw new IllegalArgumentException("Flavor, size, category, and availability are required.");
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Price must be a valid non-negative amount.");
        }
        if (shape < 0 || shape > 3) {
            throw new IllegalArgumentException("Choose a valid item shape.");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Please choose an item image.");
        }
        if (imageFile.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("The image must be 5 MB or smaller.");
        }

        String extension = switch (imageFile.getContentType() == null
                ? ""
                : imageFile.getContentType().toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Choose a JPG, PNG, or WEBP image.");
        };
        String imageFilename = UUID.randomUUID() + extension;
        String id = UUID.randomUUID().toString();
        Files.createDirectories(imageDirectory);
        Files.createDirectories(dataDirectory);

        Path imagePath = imageDirectory.resolve(imageFilename);
        try (var inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
        }

        String record = String.join(";",
                encode(id),
                encode(name.trim()),
                encode(description.trim()),
                encode(flavor.trim()),
                encode(size.trim()),
                encode(Double.toString(price)),
                encode(Integer.toString(shape)),
                encode(category.trim()),
                encode(availability.trim()),
                encode(imageFilename)) + System.lineSeparator();
        try {
            Files.writeString(
                    databaseFile,
                    record,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException exception) {
            Files.deleteIfExists(imagePath);
            throw exception;
        }
    }

    public boolean deleteItem(String id) throws IOException {
        List<Item> items = findAllItems();
        Item itemToDelete = items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (itemToDelete == null) {
            return false;
        }

        items.remove(itemToDelete);
        List<String> records = new ArrayList<>(items.size());
        for (Item item : items) {
            String imageFilename = item.getImageUrl().substring(IMAGE_URL_PREFIX.length());
            records.add(String.join(";",
                    encode(item.getId()),
                    encode(item.getName()),
                    encode(item.getDescription()),
                    encode(item.getFlavor()),
                    encode(item.getSize()),
                    encode(Double.toString(item.getPrice())),
                    encode(Integer.toString(item.getShape())),
                    encode(item.getCategory()),
                    encode(item.getAvailability()),
                    encode(imageFilename)));
        }

        Files.write(
                databaseFile,
                records,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        String imageFilename = itemToDelete.getImageUrl().substring(IMAGE_URL_PREFIX.length());
        Files.deleteIfExists(imageDirectory.resolve(imageFilename).normalize());
        return true;
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) throws IOException {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid item record in " + databaseFile, exception);
        }
    }
}
