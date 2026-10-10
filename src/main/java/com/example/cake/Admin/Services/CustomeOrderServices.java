package com.example.cake.Admin.Services;

import com.example.cake.Admin.Model.Custome;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomeOrderServices {
//    private static final String IMAGE_DIR = "C:/cake_app/images/";
    private static final String IMAGE_URL_PREFIX = "/custom-order-images/";

    private final Path databaseFile;
    //    private final Path imageDirectory;
    public CustomeOrderServices(
            @Value("${cake.custom-order-data-dir:src/main/resources/static/custome_order}")
            String dataDirectory) {
        this.databaseFile = Path.of(dataDirectory).toAbsolutePath().normalize()
                .resolve("custome_order.txt");
    }
    // find all custom orders from the database file and return them as a list of Custome objects
    public List<Custome> findAllCustomeOrders() throws IOException {
        if (!Files.exists(databaseFile)) {
            return List.of();
        }

        List<Custome> customeOrders = new ArrayList<>();
        for (String line : Files.readAllLines(databaseFile, StandardCharsets.UTF_8)) {
            if (line.isBlank()) {
                continue;
            }
            // Split the line into fields using semicolon as the delimiter, allowing for empty fields
            String[] fields = line.split(";", -1);
            if (fields.length != 8) {
                throw new IOException("Invalid custom order record in " + databaseFile);
            }// Create a new Custome object and add it to the list
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
    // Decode a Base64-encoded string and return the decoded value
    private String decode(String value) throws IOException {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid custom order record in " + databaseFile, exception);
        }
    }
}
