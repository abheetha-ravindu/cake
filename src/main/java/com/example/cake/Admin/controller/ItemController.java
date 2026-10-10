package com.example.cake.Admin.controller;

import com.example.cake.Admin.Model.Item;
import com.example.cake.Admin.Services.ItemService;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/item")
    public String items(Model model) throws IOException {
        model.addAttribute("items", itemService.findAllItems());
        return "Admin/static/item";
    }

    @GetMapping("/api/items")
    @ResponseBody
    public List<Item> storefrontItems() throws IOException {
        return itemService.findAllItems().stream()
                .filter(item -> "available".equalsIgnoreCase(item.getAvailability().trim()))
                .toList();
    }

    @PostMapping("/admin/items/upload")
    public String uploadItem(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String flavor,
            @RequestParam String size,
            @RequestParam String price,
            @RequestParam String shape,
            @RequestParam String category,
            @RequestParam String availability,
            @RequestParam("imageFile") MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            double parsedPrice = Double.parseDouble(price);
            int parsedShape = Integer.parseInt(shape);
            itemService.saveItem(
                    name,
                    description,
                    flavor,
                    size,
                    parsedPrice,
                    parsedShape,
                    category,
                    availability,
                    imageFile);
            redirectAttributes.addFlashAttribute("itemMessage", "Item added successfully.");
            redirectAttributes.addFlashAttribute("itemMessageType", "success");
        } catch (NumberFormatException exception) {
            redirectAttributes.addFlashAttribute("itemMessage", "Enter a valid price and shape.");
            redirectAttributes.addFlashAttribute("itemMessageType", "error");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("itemMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("itemMessageType", "error");
        } catch (IOException exception) {
            logger.error("Failed to save bakery item", exception);
            redirectAttributes.addFlashAttribute("itemMessage", "The item could not be saved. Please try again.");
            redirectAttributes.addFlashAttribute("itemMessageType", "error");
        }
        return "redirect:/item";
    }

    @PostMapping("/admin/items/delete")
    public String deleteItem(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            if (itemService.deleteItem(id)) {
                redirectAttributes.addFlashAttribute("itemMessage", "Item deleted successfully.");
                redirectAttributes.addFlashAttribute("itemMessageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("itemMessage", "Item was not found.");
                redirectAttributes.addFlashAttribute("itemMessageType", "error");
            }
        } catch (IOException exception) {
            logger.error("Failed to delete bakery item with id {}", id, exception);
            redirectAttributes.addFlashAttribute("itemMessage", "The item could not be deleted. Please try again.");
            redirectAttributes.addFlashAttribute("itemMessageType", "error");
        }
        return "redirect:/item";
    }
}
