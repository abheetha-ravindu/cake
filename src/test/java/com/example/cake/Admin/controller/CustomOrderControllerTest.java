package com.example.cake.Admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.cake.Admin.Model.Custome;
import com.example.cake.Admin.Services.CustomeOrderServices;
import com.example.cake.Admin.Services.FileDatabaseServices;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomOrderControllerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsAndReadsCustomOrderWithComments() throws Exception {
        Path dataDirectory = temporaryDirectory.resolve("custom-orders");
        CustomOrderController controller = new CustomOrderController(
                new CustomeOrderServices(new ObjectMapper(), dataDirectory.toString()));
        Custome order = validOrder();
        order.setComments("Call customer to confirm the decoration.");

        var response = controller.createCustomOrder(order);
        List<Custome> savedOrders = controller.getCustomOrders().getBody();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, savedOrders.size());
        assertEquals("Call customer to confirm the decoration.", savedOrders.get(0).getComments());
        assertEquals(List.of("Fresh Berries", "Gold Leaf"), savedOrders.get(0).getToppings());
        assertTrue(Files.exists(dataDirectory.resolve("custome_order.txt")));
    }

    @Test
    void updatesExistingCustomOrderAndKeepsItsId() throws Exception {
        Path dataDirectory = temporaryDirectory.resolve("updated-custom-orders");
        CustomeOrderServices services = new CustomeOrderServices(
                new ObjectMapper(), dataDirectory.toString());
        Custome original = validOrder();
        original.setComments("Original comment");
        Custome savedOrder = services.saveCustomeOrder(original);
        savedOrder.setName("Updated customer");
        savedOrder.setComments("Updated comment");
        savedOrder.setToppings(List.of("Fresh Berries"));

        assertTrue(services.updateCustomeOrder(savedOrder));

        List<Custome> updatedOrders = services.findAllCustomeOrders();
        assertEquals(1, updatedOrders.size());
        assertEquals(savedOrder.getId(), updatedOrders.get(0).getId());
        assertEquals("Updated customer", updatedOrders.get(0).getName());
        assertEquals("Updated comment", updatedOrders.get(0).getComments());
        assertEquals(List.of("Fresh Berries"), updatedOrders.get(0).getToppings());
    }

    @Test
    void updatesOrderThroughAdminFormAndSplitsToppings() throws Exception {
        CustomeOrderServices services = new CustomeOrderServices(
                new ObjectMapper(), temporaryDirectory.resolve("admin-updated-orders").toString());
        Custome savedOrder = services.saveCustomeOrder(validOrder());
        Admin_pannal_Controller adminController = new Admin_pannal_Controller(
                mock(FileDatabaseServices.class), services);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        mockMvc.perform(post("/admin/custom-orders/update")
                        .param("id", savedOrder.getId())
                        .param("name", "Updated Aisha")
                        .param("email", "aisha@example.com")
                        .param("phoneNumber", "+1 (555) 210-8421")
                        .param("occasion", "Birthday")
                        .param("requiredDate", LocalDate.now().plusDays(2).toString())
                        .param("fulfillmentMethod", "Pickup")
                        .param("toppings", "Fresh Berries, Gold Leaf"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/custome_order"))
                .andExpect(flash().attribute("customOrderMessage", "Custom order updated successfully."));

        Custome updatedOrder = services.findAllCustomeOrders().get(0);
        assertEquals("Updated Aisha", updatedOrder.getName());
        assertEquals(List.of("Fresh Berries", "Gold Leaf"), updatedOrder.getToppings());
    }

    @Test
    void deletesCustomOrderAndItsReferenceImage() throws Exception {
        Path dataDirectory = temporaryDirectory.resolve("deleted-custom-orders");
        CustomeOrderServices services = new CustomeOrderServices(
                new ObjectMapper(), dataDirectory.toString());
        Custome savedOrder = services.saveCustomeOrder(validOrder(),
                new MockMultipartFile("referenceImage", "cake.png", "image/png", new byte[] { 1, 2, 3 }));
        Path savedImage = dataDirectory.resolve("images")
                .resolve(savedOrder.getImageUrl().substring("/custom-order-images/".length()));
        assertTrue(Files.exists(savedImage));

        assertTrue(services.deleteCustomeOrder(savedOrder.getId()));

        assertTrue(services.findAllCustomeOrders().isEmpty());
        assertTrue(Files.readString(dataDirectory.resolve("custome_order.txt")).isEmpty());
        assertTrue(Files.notExists(savedImage));
    }

    @Test
    void deletesCustomOrderThroughAdminForm() throws Exception {
        Path dataDirectory = temporaryDirectory.resolve("admin-deleted-orders");
        CustomeOrderServices services = new CustomeOrderServices(
                new ObjectMapper(), dataDirectory.toString());
        Custome savedOrder = services.saveCustomeOrder(validOrder());
        Admin_pannal_Controller adminController = new Admin_pannal_Controller(
                mock(FileDatabaseServices.class), services);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        mockMvc.perform(post("/admin/custom-orders/delete").param("id", savedOrder.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/custome_order"))
                .andExpect(flash().attribute("customOrderMessage", "Custom order deleted successfully."));

        assertTrue(services.findAllCustomeOrders().isEmpty());
    }

    @Test
    void rejectsDeliveryOrderWithoutAddress() {
        CustomeOrderServices services = new CustomeOrderServices(
                new ObjectMapper(), temporaryDirectory.resolve("custom-orders").toString());
        CustomOrderController controller = new CustomOrderController(services);
        Custome order = validOrder();
        order.setFulfillmentMethod("Delivery");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> controller.createCustomOrder(order));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void submitsNativeFormOrderToSharedCustomOrderFile() throws Exception {
        Path dataDirectory = temporaryDirectory.resolve("native-form-orders");
        CustomOrderController controller = new CustomOrderController(
                new CustomeOrderServices(new ObjectMapper(), dataDirectory.toString()));
        Custome order = validOrder();
        order.setName("<Aisha Rahman>");
        order.setAddress("");
        order.setComments("Please call & confirm before delivery.");
        MockMultipartFile referenceImage = new MockMultipartFile(
                "referenceImage", "cake.png", "image/png", new byte[] { 1, 2, 3 });

        var response = controller.submitCustomOrder(order, referenceImage);
        Custome savedOrder = controller.getCustomOrders().getBody().get(0);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.TEXT_HTML, response.getHeaders().getContentType());
        assertTrue(response.getBody().contains(savedOrder.getId()));
        assertTrue(response.getBody().contains("&lt;Aisha Rahman&gt;"));
        assertTrue(response.getBody().contains("Please call &amp; confirm before delivery."));
        assertTrue(response.getBody().contains("Round"));
        assertTrue(response.getBody().contains("Vanilla"));
        assertTrue(response.getBody().contains("Birthday"));
        assertTrue(response.getBody().contains("To be confirmed"));
        assertEquals("Please call & confirm before delivery.", savedOrder.getComments());
        assertEquals("To be confirmed", savedOrder.getEstimatedTotal());
        assertEquals(LocalDate.now().toString(), savedOrder.getOrderDate());
        assertTrue(savedOrder.getImageUrl().startsWith("/custom-order-images/"));
        assertTrue(Files.exists(dataDirectory.resolve("images")
                .resolve(savedOrder.getImageUrl().substring("/custom-order-images/".length()))));
    }

    private Custome validOrder() {
        Custome order = new Custome();
        order.setName("Aisha Rahman");
        order.setEmail("aisha@example.com");
        order.setPhoneNumber("+1 (555) 210-8421");
        order.setRequiredDate(LocalDate.now().plusDays(2).toString());
        order.setFulfillmentMethod("Pickup");
        order.setOccasion("Birthday");
        order.setCakeShape("Round");
        order.setCakeFlavor("Vanilla");
        order.setCakeSize("Medium");
        order.setToppings(List.of("Fresh Berries", "Gold Leaf"));
        return order;
    }
}
