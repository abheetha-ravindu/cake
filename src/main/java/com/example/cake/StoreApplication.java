package com.example.cake;

//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StoreApplication {
    public static void main(String[] args) {
        // Start the Spring Boot application so beans and controllers are initialized correctly.
        //SpringApplication.run(StoreApplication.class, args);

        var orderServices = new OrderService(new PayPalPaymentServices());
        orderServices.placeOrder();
    }
}
