package com.example.cake.repository;

import java.util.List;

import com.example.cake.model.User;

/**
 * Storage contract for users. The service depends on this interface rather than
 * knowing whether users are stored in a text file, a database, or another system.
 */
public interface UserRepository {
    /** Loads all stored users; returns an empty list when there are no records. */
    List<User> findAll();

    /** Persists one user record. */
    void save(User user);
}
