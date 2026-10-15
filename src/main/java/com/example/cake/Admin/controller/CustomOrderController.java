package com.example.cake.Admin.controller;

import com.example.cake.Admin.Model.Custome;
import com.example.cake.Admin.Services.CustomeOrderServices;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/custom-orders")
public class CustomOrderController {

    private final CustomeOrderServices customeOrderServices;

    public CustomOrderController(CustomeOrderServices customeOrderServices) {
        this.customeOrderServices = customeOrderServices;
    }

    @GetMapping
    public ResponseEntity<List<Custome>> getCustomOrders() throws IOException {
        return ResponseEntity.ok(customeOrderServices.findAllCustomeOrders());
    }

    @PostMapping
    public ResponseEntity<Custome> createCustomOrder(@RequestBody Custome order) throws IOException {
        validateOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customeOrderServices.saveCustomeOrder(order));
    }

    @PostMapping(value = "/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitCustomOrder(
            @ModelAttribute Custome order,
            @RequestParam(value = "referenceImage", required = false) MultipartFile referenceImage)
            throws IOException {
        if (isBlank(order.getOrderDate())) {
            order.setOrderDate(LocalDate.now().toString());
        }
        if (isBlank(order.getEstimatedTotal())) {
            order.setEstimatedTotal("To be confirmed");
        }
        validateOrder(order);
        Custome savedOrder = customeOrderServices.saveCustomeOrder(order, referenceImage);
        String receiptHeader = """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Custom cake order receipt</title>
                  <style>
                    body{margin:0;padding:2rem;background:#fdf8f2;color:#2c1e16;font:16px Arial,sans-serif}
                    main{max-width:760px;margin:2rem auto;padding:2rem;background:#fff;border:1px solid #eadbce;border-radius:20px;box-shadow:0 12px 36px #4e301a18}
                    h1{color:#fd344a}p{line-height:1.5}.receipt{width:100%;border-collapse:collapse;margin:1.5rem 0}
                    th,td{padding:.8rem;text-align:left;vertical-align:top;border-bottom:1px solid #eadbce}
                    th{width:34%;color:#5e4d43}a{color:#fd344a}
                  </style>
                </head>
                <body>
                  <main>
                    <h1>Thank you. Your custom cake order has been received.</h1>
                    <p>Order reference: <strong>{{orderReference}}</strong></p>
                    <table class="receipt"><tbody>
                """;
        StringBuilder receipt = new StringBuilder(receiptHeader.replace(
                "{{orderReference}}", escapeHtml(savedOrder.getId())));
        addReceiptRow(receipt, "Customer name", savedOrder.getName());
        addReceiptRow(receipt, "Email", savedOrder.getEmail());
        addReceiptRow(receipt, "Phone number", savedOrder.getPhoneNumber());
        addReceiptRow(receipt, "Occasion", savedOrder.getOccasion());
        addReceiptRow(receipt, "Cake shape", savedOrder.getCakeShape());
        addReceiptRow(receipt, "Flavor", savedOrder.getCakeFlavor());
        addReceiptRow(receipt, "Filling", savedOrder.getFilling());
        addReceiptRow(receipt, "Decoration", savedOrder.getDecoration());
        addReceiptRow(receipt, "Size / servings", savedOrder.getCakeSize());
        addReceiptRow(receipt, "Toppings", savedOrder.getToppings() == null
                ? "" : String.join(", ", savedOrder.getToppings()));
        addReceiptRow(receipt, "Message on cake", savedOrder.getCakeMessage());
        if (savedOrder.getImageUrl() == null || savedOrder.getImageUrl().isBlank()) {
            addReceiptRow(receipt, "Reference image", "Not provided");
        } else {
            receipt.append("<tr><th>Reference image</th><td><a href=\"")
                    .append(escapeHtml(savedOrder.getImageUrl()))
                    .append("\">View uploaded image</a></td></tr>");
        }
        addReceiptRow(receipt, "Required date", savedOrder.getRequiredDate());
        addReceiptRow(receipt, "Preferred time", savedOrder.getPreferredTime());
        addReceiptRow(receipt, "Fulfillment", savedOrder.getFulfillmentMethod());
        addReceiptRow(receipt, "Address", savedOrder.getAddress());
        addReceiptRow(receipt, "Special requests", savedOrder.getSpecialRequests());
        addReceiptRow(receipt, "Comments", savedOrder.getComments());
        addReceiptRow(receipt, "Price estimate", savedOrder.getEstimatedTotal());
        addReceiptRow(receipt, "Order date", savedOrder.getOrderDate());
        receipt.append("""
                    </tbody></table>
                    <p>Our bakery team will contact you to confirm the details.</p>
                    <a href="/custom-cake.html">Return to custom cake booking</a>
                  </main>
                </body>
                </html>
                """);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_HTML)
                .body(receipt.toString());
    }

    private void validateOrder(Custome order) {
        if (order == null || isBlank(order.getName()) || isBlank(order.getEmail())
                || isBlank(order.getPhoneNumber()) || isBlank(order.getRequiredDate())
                || isBlank(order.getFulfillmentMethod()) || isBlank(order.getOccasion())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name, email, phone, occasion, required date, and fulfillment method are required.");
        }

        try {
            LocalDate.parse(order.getRequiredDate());
        } catch (DateTimeParseException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required date must be a valid ISO date.");
        }

        if ("Delivery".equalsIgnoreCase(order.getFulfillmentMethod()) && isBlank(order.getAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A delivery address is required for delivery.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void addReceiptRow(StringBuilder receipt, String label, String value) {
        receipt.append("<tr><th>")
                .append(escapeHtml(label))
                .append("</th><td>")
                .append(isBlank(value) ? "Not provided" : escapeHtml(value))
                .append("</td></tr>");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
