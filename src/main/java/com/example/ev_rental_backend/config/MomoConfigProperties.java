package com.example.ev_rental_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "momo")
@Data
public class MomoConfigProperties {

    /**
     * Mã đối tác được MoMo cấp
     * Test: MOMOBKUN20180529
     */
    private String partnerCode;

    /**
     * Access Key từ MoMo
     * Test: klm05TvNBzhg7h7j
     */
    private String accessKey;

    /**
     * Secret Key để ký HMAC SHA256
     * Test: at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
     */
    private String secretKey;

    /**
     * Endpoint API của MoMo
     * Test: https://test-payment.momo.vn/v2/gateway/api/create
     * Production: https://payment.momo.vn/v2/gateway/api/create
     */
    private String endpoint;

    /**
     * URL redirect sau khi thanh toán (trên web)
     * User sẽ được redirect về đây sau khi thanh toán
     */
    private String redirectUrl;

    /**
     * IPN URL - Webhook để MoMo gọi về backend
     * MoMo sẽ gọi POST request đến URL này để thông báo kết quả
     */
    private String ipnUrl;

    /**
     * Request type:
     * - captureWallet: Thanh toán qua ví MoMo
     * - payWithATM: Thanh toán qua thẻ ATM
     */
    private String requestType = "captureWallet";

    /**
     * Timeout cho HTTP request (milliseconds)
     */
    private int timeout = 30000; // 30 seconds
}
