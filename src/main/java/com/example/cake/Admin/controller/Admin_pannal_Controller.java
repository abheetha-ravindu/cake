package com.example.cake.Admin.controller;

import com.example.cake.Admin.Model.Custome;
import com.example.cake.Admin.Model.UserDetails;
import com.example.cake.Admin.Services.CustomeOrderServices;
import com.example.cake.Admin.Services.FileDatabaseServices;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Admin_pannal_Controller {
    private static final Logger logger = LoggerFactory.getLogger(Admin_pannal_Controller.class);
    private final FileDatabaseServices fileDatabaseServices;
    private final CustomeOrderServices customeOrderServices;

    public Admin_pannal_Controller(
            FileDatabaseServices fileDatabaseServices,
            CustomeOrderServices customeOrderServices) {
        this.fileDatabaseServices = fileDatabaseServices;
        this.customeOrderServices = customeOrderServices;
    }

    @GetMapping({"/dashboard", "/dashboardboard"})
    public String dashboard() {
        return "forward:/Admin/static/dashboard.html";
    }

    @GetMapping({"/custome_order", "/custom_order"})
    public String customOrder(Model model) throws IOException {
        model.addAttribute("customOrders", customeOrderServices.findAllCustomeOrders());
        return "Admin/static/custome_order";
    }

    @PostMapping("/admin/custom-orders/update")
    public String updateCustomOrder(@ModelAttribute Custome order, RedirectAttributes redirectAttributes)
            throws IOException {
        if (isBlank(order.getName()) || isBlank(order.getEmail()) || isBlank(order.getPhoneNumber())
                || isBlank(order.getRequiredDate()) || isBlank(order.getFulfillmentMethod())
                || isBlank(order.getOccasion())) {
            redirectAttributes.addFlashAttribute("customOrderMessage",
                    "Name, email, phone, occasion, required date, and fulfillment method are required.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
        } else if (!isValidDate(order.getRequiredDate())) {
            redirectAttributes.addFlashAttribute("customOrderMessage", "Required date must be a valid date.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
        } else if ("Delivery".equalsIgnoreCase(order.getFulfillmentMethod()) && isBlank(order.getAddress())) {
            redirectAttributes.addFlashAttribute("customOrderMessage", "A delivery address is required for delivery.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
        } else if (customeOrderServices.updateCustomeOrder(order)) {
            redirectAttributes.addFlashAttribute("customOrderMessage", "Custom order updated successfully.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("customOrderMessage",
                    "This custom order was not found; no changes were made.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
        }
        return "redirect:/custome_order";
    }

    @PostMapping("/admin/custom-orders/delete")
    public String deleteCustomOrder(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            if (customeOrderServices.deleteCustomeOrder(id)) {
                redirectAttributes.addFlashAttribute("customOrderMessage", "Custom order deleted successfully.");
                redirectAttributes.addFlashAttribute("customOrderMessageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("customOrderMessage",
                        "Custom order was not found; no changes were made.");
                redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
            }
        } catch (IOException exception) {
            logger.error("Failed to delete custom order with id {}", id, exception);
            redirectAttributes.addFlashAttribute("customOrderMessage",
                    "The custom order could not be deleted completely. Please check its saved image and try again.");
            redirectAttributes.addFlashAttribute("customOrderMessageType", "error");
        }
        return "redirect:/custome_order";
    }
    
    @GetMapping({"/normal_orders", "/normal_order"})
    public String normalOrders() {
        return "forward:/Admin/static/normal_order.html";
    }

    @GetMapping("/order_details")
    public String orderDetails(@RequestParam("orderId") String orderId) {
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

    private boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
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
