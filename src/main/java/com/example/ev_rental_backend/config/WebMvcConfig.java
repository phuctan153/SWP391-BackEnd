package com.example.ev_rental_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        System.out.println("Serving static files from: " + uploadPath);

        // ✅ Chỉ public ảnh phương tiện, avatar
        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + uploadPath + "images/")
                .setCachePeriod(0);

        registry.addResourceHandler("/files/vehicles/**")
                .addResourceLocations("file:" + uploadPath + "vehicles/")
                .setCachePeriod(0);

        // 🚫 KHÔNG public hợp đồng để tránh lộ file PDF
        // /files/contracts/** sẽ được bảo vệ qua API có JWT
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://swp-391-frontend-mu.vercel.app",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "https://nonpending-lelia-ballistically.ngrok-free.dev"
                )
                .allowedHeaders("*") // ✅ Cho phép tất cả header
                .exposedHeaders("Authorization", "Content-Type")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }

}
