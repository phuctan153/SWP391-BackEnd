package com.example.ev_rental_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Cấu hình đường dẫn tới thư mục uploads
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        System.out.println("Serving static files from: " + uploadPath);

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://swp-391-frontend-mu.vercel.app",
                        "https://localhost:3000",
                        "http://localhost:8080",
                        "https://nonpending-lelia-ballistically.ngrok-free.dev"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
