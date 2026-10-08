package com.example.cake.model;

/**
 * One user's data as it passes between the service and repository.
 * A record provides immutable fields and generated accessors such as {@code id()}.
 */
public record User(long id, String username, String password, String email,String user_type) {
}
