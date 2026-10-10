package com.example.cake.Admin.Services;

import com.example.cake.Admin.Model.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileDatabaseServices {
//Get the file path for the database file
    private Path getFilePath() throws IOException {
        return Paths.get("database.txt").toAbsolutePath().normalize();
    }
//Save user to file
    public void saveUser(UserDetails userDetails) throws IOException {
        Path path = getFilePath();
        String line = userDetails.getId() + "," + userDetails.getName() + "," + userDetails.getPassword() + "," + userDetails.getEmail();
        if (userDetails.getUser_type() != null && !userDetails.getUser_type().isBlank()) {
            line += "," + userDetails.getUser_type();
        }
        Files.write(path, List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
//Retrieve all users
    public List<UserDetails> findAllUsers() throws IOException {
        Path filePath = getFilePath();
        if (!Files.exists(filePath)) {
            return Collections.emptyList();
        }
        return Files.lines(filePath)
                .filter(line -> !line.trim().isEmpty())
                .map(line -> {
                    String[] parts = line.split(",", 5);
                    String id = parts.length > 0 ? parts[0].trim() : "";
                    String name = parts.length > 1 ? parts[1].trim() : "";
                    String password = parts.length > 2 ? parts[2].trim() : "";
                    String email = parts.length > 3 ? parts[3].trim() : "";
                    String userType = parts.length > 4 ? parts[4].trim() : "Customer";
                    return new UserDetails(id, name, email, password, userType);
                })
                .collect(Collectors.toList());
    }
//Update user by ID
    public boolean updateUser(String id, UserDetails userDetails) throws IOException {
        List<UserDetails> users = findAllUsers();
        boolean found = false;
        for (UserDetails updateUser : users) {
            if (id.equals(updateUser.getId())) {
                updateUser.setName(userDetails.getName());
                updateUser.setEmail(userDetails.getEmail());
                if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
                    updateUser.setPassword(userDetails.getPassword());
                }
                updateUser.setUser_type(userDetails.getUser_type());
                found = true;
                break;
            }
        }
        if (found) {
            rewriteFile(users);
        }
        return found;
    }
//Delete user by ID
    public boolean delete(String id) throws IOException {
        List<UserDetails> users = findAllUsers();
        int initialSize = users.size();
        users.removeIf(user -> user.getId().equals(id));

        if (users.size() < initialSize) {
            rewriteFile(users);
            return true;
        }
        return false;
    }
//Rewrite the file with updated user data
    private void rewriteFile(List<UserDetails> users) throws IOException {
        Path path = getFilePath();
        List<String> lines = users.stream()
                .map(user -> user.getId() + "," + user.getName() + "," + user.getPassword() + "," + user.getEmail() + "," + user.getUser_type())
                .collect(Collectors.toList());
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }
}
