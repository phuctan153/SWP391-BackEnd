package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.config.PayOSConfigProperties;
import com.example.ev_rental_backend.dto.payos.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSPaymentService {

    private final PayOSConfigProperties payosConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Tạo payment link với PayOS
     *
     * @param transactionId ID transaction trong hệ thống
     * @param amount Số tiền (VND)
     * @param description Mô tả
     * @param buyerName Tên người mua
     * @param buyerEmail Email
     * @return Payment info
     */
    public PayOSPaymentInfoDto createPayment(Long transactionId, Integer amount,
                                             String description, String buyerName,
                                             String buyerEmail) {
        try {
            // 1. Tạo order code unique (dùng timestamp)
            Long orderCode = System.currentTimeMillis();

            log.info("Creating PayOS payment - OrderCode: {}, Amount: {}", orderCode, amount);

            // 2. Tạo items (PayOS yêu cầu phải có ít nhất 1 item)
            List<PayOSItem> items = new ArrayList<>();
            items.add(PayOSItem.builder()
                    .name(description)
                    .quantity(1)
                    .price(amount)
                    .build());

            // 3. Thời gian hết hạn (15 phút)
            Long expiredAt = Instant.now().getEpochSecond() + 900; // 15 minutes

            // 4. Tạo request body
            PayOSCreatePaymentRequest request = PayOSCreatePaymentRequest.builder()
                    .orderCode(orderCode)
                    .amount(amount)
                    .description(description)
                    .buyerName(buyerName)
                    .buyerEmail(buyerEmail)
                    .items(items)
                    .returnUrl(payosConfig.getReturnUrl())
                    .cancelUrl(payosConfig.getCancelUrl())
                    .expiredAt(expiredAt)
                    .build();

            // 5. Generate signature
            String signature = generateSignature(request);
            request.setSignature(signature);

            log.debug("PayOS request: {}", request);

            // 6. Gọi PayOS API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", payosConfig.getClientId());
            headers.set("x-api-key", payosConfig.getApiKey());

            HttpEntity<PayOSCreatePaymentRequest> entity = new HttpEntity<>(request, headers);

            String url = payosConfig.getEndpoint() + "/v2/payment-requests";

            ResponseEntity<PayOSCreatePaymentResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PayOSCreatePaymentResponse.class
            );

            PayOSCreatePaymentResponse response = responseEntity.getBody();

            // 7. Kiểm tra response
            if (response == null) {
                throw new CustomException("PayOS response is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("PayOS response - Code: {}, Desc: {}", response.getCode(), response.getDesc());

            // 8. Parse response
            if ("00".equals(response.getCode())) {
                // Success
                PayOSPaymentData data = response.getData();

                return PayOSPaymentInfoDto.builder()
                        .transactionId(transactionId)
                        .orderCode(orderCode)
                        .checkoutUrl(data.getCheckoutUrl())
                        .qrCode(data.getQrCode())
                        .amount(amount)
                        .status("PENDING")
                        .message("Payment created successfully")
                        .build();
            } else {
                // Error
                log.error("PayOS payment creation failed - Code: {}, Desc: {}",
                        response.getCode(), response.getDesc());

                return PayOSPaymentInfoDto.builder()
                        .transactionId(transactionId)
                        .message("Failed to create payment: " + response.getDesc())
                        .status("FAILED")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error creating PayOS payment", e);
            throw new CustomException("Error creating PayOS payment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verify webhook signature từ PayOS
     *
     * @param webhookData Webhook data
     * @return true nếu hợp lệ
     */
    public boolean verifyWebhookSignature(PayOSWebhookRequest webhookData) {
        try {
            // Build data string for signature
            PayOSWebhookData data = webhookData.getData();
            String dataStr = String.format(
                    "amount=%d&code=%s&desc=%s&orderCode=%d",
                    data.getAmount(),
                    data.getCode(),
                    data.getDesc(),
                    data.getOrderCode()
            );

            // Generate signature
            String calculatedSignature = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

            // Compare
            boolean isValid = calculatedSignature.equals(webhookData.getSignature());

            if (!isValid) {
                log.error("Invalid PayOS webhook signature - Expected: {}, Received: {}",
                        calculatedSignature, webhookData.getSignature());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying PayOS webhook signature", e);
            return false;
        }
    }

    /**
     * Tạo response cho PayOS webhook
     */
    public PayOSWebhookResponse createWebhookResponse(boolean success, String message) {
        return PayOSWebhookResponse.builder()
                .error(success ? 0 : 1)
                .message(message)
                .data(null)
                .build();
    }

    /**
     * Parse transaction ID từ order code
     * OrderCode format: timestamp (correlate với transactionId trong DB)
     */
    public Long parseTransactionIdFromOrderCode(Long orderCode) {
        // Trong production, nên lưu mapping orderCode -> transactionId vào DB
        // Ở đây đơn giản hóa: query từ DB bằng orderCode
        return orderCode; // Placeholder
    }

    // ==================== Private Helper Methods ====================

    /**
     * Generate signature cho PayOS request
     */
    private String generateSignature(PayOSCreatePaymentRequest request) {
        try {
            // Build signature string theo PayOS docs
            StringBuilder sb = new StringBuilder();
            sb.append("amount=").append(request.getAmount());
            sb.append("&cancelUrl=").append(request.getCancelUrl());
            sb.append("&description=").append(request.getDescription());
            sb.append("&orderCode=").append(request.getOrderCode());
            sb.append("&returnUrl=").append(request.getReturnUrl());

            String data = sb.toString();
            return generateHMACSHA256(data, payosConfig.getChecksumKey());

        } catch (Exception e) {
            log.error("Error generating PayOS signature", e);
            throw new RuntimeException("Error generating signature", e);
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String generateHMACSHA256(String data, String key) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmacSHA256.init(secretKeySpec);

            byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            log.error("Error generating HMAC SHA256", e);
            throw new RuntimeException("Error generating signature", e);
        }
    }
}
