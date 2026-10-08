package com.example.cake.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.cake.service.DuplicateUserException;
import com.example.cake.service.UserService;

/**
 * Handles browser routes: it accepts HTTP input, delegates signup rules to
 * {@link UserService}, and chooses the page or redirect returned to the browser.
 */
@Controller
public class WebController {
    private final UserService userService;

    /**
     * Spring supplies the service so this controller does not implement business
     * rules or access the user database directly.
     */
    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        
        // Forward the root route to the static home page.
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/signup.html";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        try {
            // The service validates the fields, checks for duplicates, and saves the new user.
            
            userService.register(username, email, password);
        
        } catch (DuplicateUserException exception) {
            // Convert expected signup failures into HTTP responses the browser can understand.
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage(), exception);
        
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        
        }
        return "redirect:/signup?success";
    }
}
