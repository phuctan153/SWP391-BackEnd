package com.example.ev_rental_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payos")
@Data
public class PayOSConfigProperties {

    /**
     * Client ID từ PayOS Dashboard
     * Test: Lấy từ https://my.payos.vn/
     */
    private String clientId;

    /**
     * API Key từ PayOS
     */
    private String apiKey;

    /**
     * Checksum Key để verify webhook
     */
    private String checksumKey;

    /**
     * Endpoint API của PayOS
     * Test: https://api-merchant.payos.vn
     * Production: https://api-merchant.payos.vn
     */
    private String endpoint = "https://api-merchant.payos.vn";

    /**
     * Return URL - URL redirect sau khi thanh toán (Frontend)
     */
    private String returnUrl;

    /**
     * Cancel URL - URL khi user hủy thanh toán
     */
    private String cancelUrl;

    /**
     * Webhook URL - PayOS gọi về backend để thông báo kết quả
     * QUAN TRỌNG: Phải là public URL, không dùng localhost
     */
    private String webhookUrl;

    /**
     * Timeout cho HTTP request (milliseconds)
     */
    private int timeout = 30000; // 30 seconds
}
