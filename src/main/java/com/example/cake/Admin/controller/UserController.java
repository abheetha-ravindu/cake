package com.example.cake.Admin.controller;

import com.example.cake.Admin.Model.UserDetails;
import com.example.cake.Admin.Services.FileDatabaseServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class UserController {

    private final FileDatabaseServices fileDatabaseServices;

    public UserController(FileDatabaseServices fileDatabaseServices) {
        this.fileDatabaseServices = fileDatabaseServices;
    }

    @GetMapping
    public ResponseEntity<List<UserDetails>> getAllUsers() throws IOException {
        return ResponseEntity.ok(fileDatabaseServices.findAllUsers());
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserDetails userDetails) throws IOException {
        fileDatabaseServices.saveUser(userDetails);
        return ResponseEntity.ok("User saved successfully!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody UserDetails userDetails) throws IOException {
        boolean updated = fileDatabaseServices.updateUser(id, userDetails);
        if (updated) {
            return ResponseEntity.ok("User updated successfully!");
        }
        return ResponseEntity.status(404).body("User ID not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) throws IOException {
        boolean deleted = fileDatabaseServices.delete(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully!");
        }
        return ResponseEntity.status(404).body("User ID not found.");
    }
}
