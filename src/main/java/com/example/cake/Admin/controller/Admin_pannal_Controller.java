package com.example.cake.Admin.controller;

import com.example.cake.Admin.Services.FileDatabaseServices;
import java.io.IOException;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.cake.Admin.Model.UserDetails;


@Controller 
public class Admin_pannal_Controller {
    private final FileDatabaseServices fileDatabaseServices;

    public Admin_pannal_Controller(FileDatabaseServices fileDatabaseServices) {
        this.fileDatabaseServices = fileDatabaseServices;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/Admin/static/dashboard.html";
    }

    @GetMapping("/custome_order")
    public String customOrder() {
        return "forward:/Admin/static/custome_order.html";
    }
    
    @GetMapping({"/normal_orders", "/normal_order"})
    public String normalOrders() {
        return "forward:/Admin/static/normal_order.html";
    }
    
    
    @GetMapping("/order_details")
    public String orderDetails(@RequestParam("orderId") String orderId) {
        // You can use the orderId parameter to fetch order details from your database or service
        // For now, we will just forward to the order_details.html page
        return "forward:/Admin/static/order_details.html";
    }

    @GetMapping("/user_details")
    public String userDetails(Model model) throws IOException {
        model.addAttribute("users", fileDatabaseServices.findAllUsers());
        model.addAttribute("currentPath", "/user_details");
        return "Admin/static/user_details";
    }

    @PostMapping("/update_user")
    public String updateUser(@ModelAttribute UserDetails user, RedirectAttributes redirectAttributes)
            throws IOException {
        if (isBlank(user.getId()) || isBlank(user.getName()) || isBlank(user.getEmail())
                || isBlank(user.getUser_type())) {
            redirectAttributes.addFlashAttribute("userMessage", "Name, email, and user type are required.");
            redirectAttributes.addFlashAttribute("userMessageType", "error");
        } else if (fileDatabaseServices.updateUser(user.getId(), user)) {
            redirectAttributes.addFlashAttribute("userMessage", "User updated successfully.");
            redirectAttributes.addFlashAttribute("userMessageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("userMessage", "User was not found; no changes were made.");
            redirectAttributes.addFlashAttribute("userMessageType", "error");
        }
        return "redirect:/user_details";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @PostMapping("/delete_user")
    public String deleteUser(@RequestParam String id, RedirectAttributes redirectAttributes)
            throws IOException {
        if (fileDatabaseServices.delete(id)) {
            redirectAttributes.addFlashAttribute("userMessage", "User deleted successfully.");
            redirectAttributes.addFlashAttribute("userMessageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("userMessage", "User was not found; no changes were made.");
            redirectAttributes.addFlashAttribute("userMessageType", "error");
        }
        return "redirect:/user_details";
    }

}
