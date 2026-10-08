package com.example.cake.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import com.example.cake.repository.TextFileUserRepository;
import com.example.cake.service.UserService;

class WebControllerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void signupAppendsUserWithNextId() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        Files.writeString(databaseFile, "# users\n1,john,pass123,john@example.com\n");
        WebController controller = controllerFor(databaseFile);

        String result = controller.signup("alice", "alice@example.com", "secret123");

        assertEquals("redirect:/signup?success", result);
        assertTrue(Files.readString(databaseFile)
                .endsWith("2,alice,secret123,alice@example.com" + System.lineSeparator()));
    }

    @Test
    void signupRejectsDuplicateUsernameOrEmail() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        Files.writeString(databaseFile, "1,john,pass123,john@example.com\n");
        WebController controller = controllerFor(databaseFile);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.signup("JOHN", "different@example.com", "secret123"));

        assertEquals(409, error.getStatusCode().value());
        assertEquals("1,john,pass123,john@example.com\n", Files.readString(databaseFile));
    }

    private WebController controllerFor(Path databaseFile) {
        return new WebController(new UserService(new TextFileUserRepository(databaseFile)));
    }
}
