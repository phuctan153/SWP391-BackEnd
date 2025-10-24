package com.example.ev_rental_backend.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@SpringBootConfiguration
public class FreemarkerConfig {

    @Primary
    @Bean
    public Configuration freemarkerConfiguration() throws IOException {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);

        // 📁 Folder chứa template HTML
        config.setClassForTemplateLoading(this.getClass(), "/templates/");

        // ⚙️ Cấu hình mặc định
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLogTemplateExceptions(false);
        config.setWrapUncheckedExceptions(true);

        return config;
    }
}
