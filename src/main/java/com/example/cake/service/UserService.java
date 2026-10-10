package com.example.cake.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.cake.model.User;
import com.example.cake.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
// Set user service how to store data
    public synchronized void register(String username, String email, String password) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim();
        if (cleanUsername.isBlank() || cleanEmail.isBlank() || password == null || password.isBlank()
                || containsRecordDelimiter(cleanUsername) || containsRecordDelimiter(cleanEmail)
                || containsRecordDelimiter(password)) {
            throw new IllegalArgumentException("Invalid signup details");
        }

        List<User> existingUsers = userRepository.findAll();
        //checking user already sing up or not
        boolean duplicate = existingUsers.stream().anyMatch(user ->
                user.username().equalsIgnoreCase(cleanUsername)
                        || user.email().equalsIgnoreCase(cleanEmail));
        if (duplicate) {
            throw new DuplicateUserException("Username or email is already registered");
        }

        long nextId = existingUsers.stream()
                .mapToLong(User::id)
                .max()
                .orElse(0L) + 1L;
        userRepository.save(new User(nextId, cleanUsername, password, cleanEmail, "Customer"));
    }
// Check if the user details contain any record delimiters
    private boolean containsRecordDelimiter(String value) {
        return value.contains(",") || value.contains("\n") || value.contains("\r");
    }
}
