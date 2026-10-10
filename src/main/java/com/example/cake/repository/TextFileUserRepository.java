package com.example.cake.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.example.cake.model.User;

/**
 * File-based implementation of {@link UserRepository}.
 * Each non-comment line in the UTF-8 file stores one comma-separated user:
 * id,username,password,email,user type. Older records without a user type are
 * treated as customer accounts.
 */
@Repository
public class TextFileUserRepository implements UserRepository {
    private final Path databasePath;

    /**
     * Spring uses this constructor and reads the path from configuration.
     * If {@code cake.user-database} is not set, the file is {@code database.txt}.
     */
    @Autowired
    public TextFileUserRepository(@Value("${cake.user-database:database.txt}") String databaseFile) {
        this(Path.of(databaseFile));
    }

    /** Allows callers, including tests, to supply a {@link Path} directly. */
    public TextFileUserRepository(Path databasePath) {
        this.databasePath = databasePath;
    }

    @Override
    public List<User> findAll() {
        // A missing file means no users have been saved yet.
        if (!Files.exists(databasePath)) {
            return List.of();
        }

        try {
            List<User> users = new ArrayList<>();
            List<String> lines = Files.readAllLines(databasePath, StandardCharsets.UTF_8);
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index).trim();
                // Blank lines and lines beginning with # are ignored.
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Keep trailing empty fields so malformed records are detected correctly.
                String[] fields = line.split(",", -1);
                if (fields.length != 4 && fields.length != 5) {
                    throw new IllegalStateException("Malformed user record on line " + (index + 1));
                }
                try {

                    users.add(new User(
                            Long.parseLong(fields[0].trim()),
                            fields[1].trim(),
                            fields[2],
                            fields[3].trim(),
                            fields.length == 5 ? fields[4].trim() : "Customer"));
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException("Invalid user ID on line " + (index + 1), exception);
                }
            }
            return List.copyOf(users);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read user records from " + databasePath, exception);
        }
    }
// Save users database.text
    @Override
    public void save(User user) {
        try {
            // Create parent folders when the configured database path is nested.
            Path parent = databasePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // Separate the new record from existing text if the file lacks a final newline.
            String currentData = Files.exists(databasePath)
                    ? Files.readString(databasePath, StandardCharsets.UTF_8)
                    : "";
            String linePrefix = currentData.isEmpty() || currentData.endsWith("\n") || currentData.endsWith("\r")
                    ? ""
                    : System.lineSeparator();
            String record = linePrefix + user.id() + "," + user.username() + "," + user.password() + ","
                    + user.email() + "," + user.user_type() + System.lineSeparator();

            // Append rather than replacing earlier users.

            Files.writeString(databasePath, record, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not save the user record to " + databasePath, exception);
        }
    }
}
