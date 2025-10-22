package com.example.ev_rental_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    //    Swagger UI: http://localhost:8080/swagger-ui.html
//    OpenAPI JSON: http://localhost:8080/v3/api-docs
    @Bean
    public OpenAPI apiInfo() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập token JWT (ví dụ: Bearer eyJhbGciOiJIUzI1NiIs...)")
                        )
                )
                .info(new Info()
                        .title("EV Rental API Documentation")
                        .description("Tài liệu API cho hệ thống thuê xe điện (EV Rental System)")
                        .version("1.0.0")
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}

