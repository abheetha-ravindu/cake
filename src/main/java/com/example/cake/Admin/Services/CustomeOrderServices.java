package com.example.cake.Admin.Services;

import com.example.cake.Admin.Model.Custome;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
public class CustomeOrderServices {
    private static final String IMAGE_URL_PREFIX = "/custom-order-images/";

    private final Path databaseFile;
    private final ObjectMapper objectMapper;

    public CustomeOrderServices(
            ObjectMapper objectMapper,
            @Value("${cake.custom-order-data-dir:src/main/resources/static/custome_order}")
            String dataDirectory) {
        this.objectMapper = objectMapper;
        this.databaseFile = Path.of(dataDirectory).toAbsolutePath().normalize()
                .resolve("custome_order.txt");
    }

    public List<Custome> findAllCustomeOrders() throws IOException {
        if (!Files.exists(databaseFile)) {
            return List.of();
        }

        List<Custome> customeOrders = new ArrayList<>();
        for (String line : Files.readAllLines(databaseFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            if (line.startsWith("{")) {
                customeOrders.add(objectMapper.readValue(line, Custome.class));
                continue;
            }

            String[] fields = line.split(";", -1);
            if (fields.length != 8) {
                throw new IOException("Invalid custom order record in " + databaseFile);
            }
            customeOrders.add(new Custome(
                    decode(fields[0]),
                    decode(fields[1]),
                    decode(fields[2]),
                    IMAGE_URL_PREFIX + decode(fields[3]),
                    decode(fields[4]),
                    decode(fields[5]),
                    decode(fields[6]),
                    decode(fields[7])));
        }
        return customeOrders;
    }

    public Custome saveCustomeOrder(Custome order) throws IOException {
        return saveCustomeOrder(order, null);
    }

    public Custome saveCustomeOrder(Custome order, MultipartFile referenceImage) throws IOException {
        order.setId(UUID.randomUUID().toString());
        if (order.getDescription() == null || order.getDescription().isBlank()) {
            order.setDescription(order.getSpecialRequests());
        }
        if (order.getSpecialRequests() == null || order.getSpecialRequests().isBlank()) {
            order.setSpecialRequests(order.getDescription());
        }
        Path parent = databaseFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path savedImage = null;
        if (referenceImage != null && !referenceImage.isEmpty()) {
            String extension = switch (referenceImage.getContentType() == null
                    ? ""
                    : referenceImage.getContentType().toLowerCase(java.util.Locale.ROOT)) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Reference image must be a PNG, JPG, or WEBP file.");
            };
            if (referenceImage.getSize() > 5 * 1024 * 1024) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reference image must be 5 MB or smaller.");
            }

            Path imageDirectory = databaseFile.getParent().resolve("images");
            Files.createDirectories(imageDirectory);
            String imageFilename = UUID.randomUUID() + extension;
            savedImage = imageDirectory.resolve(imageFilename);
            try (var inputStream = referenceImage.getInputStream()) {
                Files.copy(inputStream, savedImage);
            }
            order.setImageUrl(IMAGE_URL_PREFIX + imageFilename);
        }

        try {
            Files.writeString(
                    databaseFile,
                    objectMapper.writeValueAsString(order) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException exception) {
            if (savedImage != null) {
                Files.deleteIfExists(savedImage);
            }
            throw exception;
        }
        return order;
    }

    public boolean updateCustomeOrder(Custome order) throws IOException {
        if (order.getId() == null || order.getId().isBlank()) {
            return false;
        }

        List<Custome> orders = findAllCustomeOrders();
        int orderIndex = -1;
        for (int index = 0; index < orders.size(); index++) {
            if (order.getId().equals(orders.get(index).getId())) {
                orderIndex = index;
                break;
            }
        }
        if (orderIndex < 0) {
            return false;
        }

        orders.set(orderIndex, order);
        writeOrders(orders);
        return true;
    }

    public boolean deleteCustomeOrder(String id) throws IOException {
        if (id == null || id.isBlank()) {
            return false;
        }

        List<Custome> orders = findAllCustomeOrders();
        Custome orderToDelete = orders.stream()
                .filter(order -> id.equals(order.getId()))
                .findFirst()
                .orElse(null);
        if (orderToDelete == null) {
            return false;
        }

        orders.remove(orderToDelete);
        writeOrders(orders);

        String imageUrl = orderToDelete.getImageUrl();
        if (imageUrl != null && imageUrl.startsWith(IMAGE_URL_PREFIX)) {
            String filename = imageUrl.substring(IMAGE_URL_PREFIX.length());
            if (!filename.isBlank() && !filename.contains("/") && !filename.contains("\\")
                    && !filename.equals(".") && !filename.equals("..")) {
                Path imageDirectory = databaseFile.getParent().resolve("images").normalize();
                Files.deleteIfExists(imageDirectory.resolve(filename).normalize());
            }
        }
        return true;
    }

    private void writeOrders(List<Custome> orders) throws IOException {
        Path parent = databaseFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, "custom-orders-", ".tmp");
        try {
            List<String> lines = new ArrayList<>(orders.size());
            for (Custome savedOrder : orders) {
                lines.add(objectMapper.writeValueAsString(savedOrder));
            }
            Files.write(temporaryFile, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, databaseFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, databaseFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String decode(String value) throws IOException {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid custom order record in " + databaseFile, exception);
        }
    }
}
