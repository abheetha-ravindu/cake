package com.example.cake.Admin.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.cake.Admin.Model.Item;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ItemServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void updatesItemWhileKeepingItsIdAndExistingImageWhenNoReplacementIsUploaded() throws Exception {
        ItemService itemService = new ItemService(temporaryDirectory.toString());
        itemService.saveItem("Old name", "Old description", "Vanilla", "8 inch",
                25.0, 0, "Cake", "Available", image("original.png"));
        Item originalItem = itemService.findAllItems().get(0);
        String originalImage = originalItem.getImageUrl();

        boolean updated = itemService.updateItem(
                originalItem.getId(), "New name", "New description", "Chocolate", "10 inch",
                35.5, 1, "Celebration", "Unavailable", null);

        List<Item> items = itemService.findAllItems();
        assertTrue(updated);
        assertEquals(1, items.size());
        assertEquals(originalItem.getId(), items.get(0).getId());
        assertEquals("New name", items.get(0).getName());
        assertEquals("New description", items.get(0).getDescription());
        assertEquals("Chocolate", items.get(0).getFlavor());
        assertEquals("10 inch", items.get(0).getSize());
        assertEquals(35.5, items.get(0).getPrice());
        assertEquals(1, items.get(0).getShape());
        assertEquals("Celebration", items.get(0).getCategory());
        assertEquals("Unavailable", items.get(0).getAvailability());
        assertEquals(originalImage, items.get(0).getImageUrl());
    }

    @Test
    void replacesOnlySelectedItemsImageAndRemovesTheOldImage() throws Exception {
        ItemService itemService = new ItemService(temporaryDirectory.toString());
        itemService.saveItem("Cake", "Description", "Vanilla", "8 inch",
                25.0, 0, "Cake", "Available", image("old.png"));
        Item originalItem = itemService.findAllItems().get(0);
        Path originalImage = imagePath(originalItem.getImageUrl());

        assertTrue(itemService.updateItem(
                originalItem.getId(), originalItem.getName(), originalItem.getDescription(),
                originalItem.getFlavor(), originalItem.getSize(), originalItem.getPrice(),
                originalItem.getShape(), originalItem.getCategory(), originalItem.getAvailability(),
                image("new.png")));

        Item updatedItem = itemService.findAllItems().get(0);
        Path updatedImage = imagePath(updatedItem.getImageUrl());
        assertNotEquals(originalItem.getImageUrl(), updatedItem.getImageUrl());
        assertFalse(Files.exists(originalImage));
        assertTrue(Files.exists(updatedImage));
    }

    @Test
    void returnsFalseWhenUpdatingAnUnknownItem() throws Exception {
        ItemService itemService = new ItemService(temporaryDirectory.toString());

        assertFalse(itemService.updateItem("missing-id", "Cake", "Description", "Vanilla",
                "8 inch", 25.0, 0, "Cake", "Available", null));
    }

    private MockMultipartFile image(String filename) {
        return new MockMultipartFile("imageFile", filename, "image/png", new byte[] { 1, 2, 3 });
    }

    private Path imagePath(String imageUrl) {
        return temporaryDirectory.resolve("images")
                .resolve(imageUrl.substring("/item-images/".length()));
    }
}
