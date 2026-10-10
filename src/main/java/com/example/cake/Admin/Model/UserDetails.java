package com.example.cake.Admin.Model;


public class UserDetails {
    private String id;
    private String name;
    private String email;
    private String password;
    private String user_type;

    // Constructors
    public UserDetails() {}

    public UserDetails(String id, String name, String email, String password, String user_type) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;   
        this.user_type = user_type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUser_type() { return user_type; }
    public void setUser_type(String user_type) { this.user_type = user_type; }
}
