package com.example.cake.Admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.cake.Admin.Services.ItemService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ItemControllerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storefrontEndpointIncludesAvailableAndUnavailableItems() throws Exception {
        ItemService itemService = new ItemService(temporaryDirectory.toString());
        var image = new MockMultipartFile("imageFile", "cake.png", "image/png", new byte[] { 1, 2, 3 });
        itemService.saveItem("Available cake", "Description", "Vanilla", "8 inch",
                25.0, 0, "Cake", "Available", image);
        itemService.saveItem("Unavailable cake", "Description", "Chocolate", "10 inch",
                35.0, 1, "Cake", "Unavailable", image);

        var items = new ItemController(itemService).storefrontItems();

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(item -> "Available".equals(item.getAvailability())));
        assertTrue(items.stream().anyMatch(item -> "Unavailable".equals(item.getAvailability())));
    }

    @Test
    void updatesItemThroughAdminFormAndKeepsCurrentImage() throws Exception {
        ItemService itemService = new ItemService(temporaryDirectory.toString());
        itemService.saveItem("Original cake", "Original description", "Vanilla", "8 inch",
                25.0, 0, "Cake", "Available",
                new MockMultipartFile("imageFile", "cake.png", "image/png", new byte[] { 1, 2, 3 }));
        var originalItem = itemService.findAllItems().get(0);
        String itemId = originalItem.getId();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ItemController(itemService)).build();

        mockMvc.perform(post("/admin/items/update")
                        .param("id", itemId)
                        .param("name", "Updated cake")
                        .param("description", "Updated description")
                        .param("flavor", "Chocolate")
                        .param("size", "10 inch")
                        .param("price", "32.50")
                        .param("shape", "1")
                        .param("category", "Celebration")
                        .param("availability", "Unavailable"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/item"))
                .andExpect(flash().attribute("itemMessage", "Item updated successfully."));

        var updatedItem = itemService.findAllItems().get(0);
        assertEquals(itemId, updatedItem.getId());
        assertEquals("Updated cake", updatedItem.getName());
        assertEquals("Updated description", updatedItem.getDescription());
        assertEquals("Chocolate", updatedItem.getFlavor());
        assertEquals(32.5, updatedItem.getPrice());
        assertEquals("Unavailable", updatedItem.getAvailability());
        assertEquals(originalItem.getImageUrl(), updatedItem.getImageUrl());
    }
}
