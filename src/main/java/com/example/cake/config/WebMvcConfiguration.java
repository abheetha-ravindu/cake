package com.example.cake.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${cake.image-dir:C:/cake_app/images/}")
    private String imageDir;

    @Value("${cake.item-data-dir:src/main/resources/static/item}")
    private String itemDataDirectory;

    @Value("${cake.custom-order-data-dir:src/main/resources/static/custome_order}")
    private String customOrderDataDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = Path.of(imageDir).toAbsolutePath().normalize().toUri().toString();
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(resourceLocation);

        String itemImageLocation = Path.of(itemDataDirectory, "images")
                .toAbsolutePath().normalize().toUri().toString();
        if (!itemImageLocation.endsWith("/")) {
            itemImageLocation += "/";
        }
        registry.addResourceHandler("/item-images/**")
                .addResourceLocations(itemImageLocation);

        String customOrderImageLocation = Path.of(customOrderDataDirectory, "images")
                .toAbsolutePath().normalize().toUri().toString();
        if (!customOrderImageLocation.endsWith("/")) {
            customOrderImageLocation += "/";
        }
        registry.addResourceHandler("/custom-order-images/**")
                .addResourceLocations(customOrderImageLocation);
    }
}
