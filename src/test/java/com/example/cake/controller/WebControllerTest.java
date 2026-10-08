package com.example.cake.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.cake.repository.TextFileUserRepository;
import com.example.cake.service.UserService;

class WebControllerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void signupAppendsUserWithNextId() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        Files.writeString(databaseFile, "# users\n1,john,pass123,john@example.com,Customer\n");
        WebController controller = controllerFor(databaseFile);

        String result = controller.signup("alice", "alice@example.com", "secret123");

        assertEquals("redirect:/signup?success", result);
        assertTrue(Files.readString(databaseFile)
                .endsWith("2,alice,secret123,alice@example.com,Customer" + System.lineSeparator()));
    }

    @Test
    void signupAlwaysAssignsCustomerRole() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        WebController controller = controllerFor(databaseFile);

        controller.signup("alice", "alice@example.com", "secret123");

        assertEquals("1,alice,secret123,alice@example.com,Customer" + System.lineSeparator(),
                Files.readString(databaseFile));
    }

    @Test
    void signupSupportsExistingRecordsWithoutUserType() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        Files.writeString(databaseFile, "4,john,pass123,john@example.com\n");
        WebController controller = controllerFor(databaseFile);

        controller.signup("alice", "alice@example.com", "secret123");

        assertTrue(Files.readString(databaseFile)
                .endsWith("5,alice,secret123,alice@example.com,Customer" + System.lineSeparator()));
    }

    @Test
    void signupRedirectsWithErrorWhenUsernameOrEmailIsAlreadyRegistered() throws Exception {
        Path databaseFile = temporaryDirectory.resolve("database.txt");
        Files.writeString(databaseFile, "1,john,pass123,john@example.com,Customer\n");
        WebController controller = controllerFor(databaseFile);

        String result = controller.signup("JOHN", "different@example.com", "secret123");

        assertEquals("redirect:/signup?error=duplicate", result);
        assertEquals("1,john,pass123,john@example.com,Customer\n", Files.readString(databaseFile));
    }

    private WebController controllerFor(Path databaseFile) {
        return new WebController(new UserService(new TextFileUserRepository(databaseFile)));
    }
}
