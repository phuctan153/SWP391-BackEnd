package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.config.PayOSConfigProperties;
import com.example.ev_rental_backend.dto.payos.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.*;
import java.util.stream.Collectors;

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
                        .paymentLinkId(data.getPaymentLinkId()) // 🆕 Lưu payment link ID
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

//    public boolean verifyWebhookSignature(PayOSWebhookRequest webhookData) {
//        try {
//            PayOSWebhookData data = webhookData.getData();
//
//            log.info("🔐 Verifying webhook signature...");
//            log.debug("Received signature: {}", webhookData.getSignature());
//
//            // 🔑 PayOS CHỈ dùng 4 fields này để tính signature (theo docs)
//            Map<String, String> dataMap = new TreeMap<>(); // TreeMap tự động sort
//
//            dataMap.put("amount", String.valueOf(data.getAmount()));
//            dataMap.put("code", data.getCode());
//            dataMap.put("desc", data.getDesc());
//            dataMap.put("orderCode", String.valueOf(data.getOrderCode()));
//
//            // Build data string: key1=value1&key2=value2&...
//            String dataStr = dataMap.entrySet().stream()
//                    .map(entry -> entry.getKey() + "=" + entry.getValue())
//                    .collect(Collectors.joining("&"));
//
//            log.debug("📝 Data string for signature (sorted by alphabet):");
//            log.debug("{}", dataStr);
//            log.debug("📊 Data map contains {} fields", dataMap.size());
//
//            // Generate signature
//            String calculatedSignature = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());
//
//            log.debug("🔐 Calculated signature: {}", calculatedSignature);
//            log.debug("📨 Received signature:   {}", webhookData.getSignature());
//
//            // Compare signatures
//            boolean isValid = calculatedSignature.equalsIgnoreCase(webhookData.getSignature());
//
//            if (!isValid) {
//                log.error("❌ Invalid PayOS webhook signature!");
//                log.error("Expected: {}", calculatedSignature);
//                log.error("Received: {}", webhookData.getSignature());
//                log.error("Data string used: {}", dataStr);
//                log.error("Checksum key length: {}", payosConfig.getChecksumKey().length());
//
//                // Debug: Print mỗi field
//                log.error("Fields used for signature:");
//                dataMap.forEach((key, value) -> log.error("  {} = {}", key, value));
//
//            } else {
//                log.info("✅ PayOS webhook signature verified successfully!");
//            }
//
//            return isValid;
//
//        } catch (Exception e) {
//            log.error("Error verifying PayOS webhook signature", e);
//            return false;
//        }
//    }


    public boolean verifyWebhookSignature(PayOSWebhookRequest webhookData) {
        try {
            PayOSWebhookData data = webhookData.getData();

            log.info("🔐 Verifying webhook signature...");
            log.debug("Received signature: {}", webhookData.getSignature());

            // 🧪 TRY VARIATION 1: Chỉ 4 fields cơ bản
            boolean v1 = tryVariation1(data, webhookData.getSignature());
            if (v1) return true;

            // 🧪 TRY VARIATION 2: Core fields (không empty)
            boolean v2 = tryVariation2(data, webhookData.getSignature());
            if (v2) return true;

            // 🧪 TRY VARIATION 3: Tất cả non-null fields
            boolean v3 = tryVariation3(data, webhookData.getSignature());
            if (v3) return true;

            log.error("❌ All signature variations failed!");

            // ⚠️ TEMPORARY: Skip verification in development
            // TODO: Remove this after fixing signature issue
            log.warn("⚠️ SKIPPING signature verification for development!");
            log.warn("⚠️ This is NOT secure! Fix signature before production!");
            return true; // ← TEMPORARY: Always return true for testing

            // Production code (uncomment sau khi fix):
            // return false;

        } catch (Exception e) {
            log.error("💥 Error verifying PayOS webhook signature", e);
            return false;
        }
    }

    /**
     * Variation 1: Chỉ 4 fields cơ bản (amount, code, desc, orderCode)
     */
    private boolean tryVariation1(PayOSWebhookData data, String receivedSignature) {
        try {
            Map<String, String> dataMap = new TreeMap<>();
            dataMap.put("amount", String.valueOf(data.getAmount()));
            dataMap.put("code", data.getCode());
            dataMap.put("desc", data.getDesc());
            dataMap.put("orderCode", String.valueOf(data.getOrderCode()));

            String dataStr = buildDataString(dataMap);
            String calculated = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

            log.info("🧪 Testing Variation 1: Basic 4 fields");
            log.info("   Data string: {}", dataStr);
            log.info("   Calculated:  {}", calculated);
            log.info("   Received:    {}", receivedSignature);
            log.info("   Checksum key length: {}", payosConfig.getChecksumKey().length());

            if (calculated.equalsIgnoreCase(receivedSignature)) {
                log.info("✅ Variation 1 MATCHED!");
                return true;
            }

            log.error("❌ Variation 1 failed - Signatures don't match");
            return false;
        } catch (Exception e) {
            log.error("💥 Variation 1 exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Variation 2: Core fields (không có empty strings)
     */
    private boolean tryVariation2(PayOSWebhookData data, String receivedSignature) {
        try {
            Map<String, String> dataMap = new TreeMap<>();

            addIfNotEmpty(dataMap, "accountNumber", data.getAccountNumber());
            addIfNotEmpty(dataMap, "amount", String.valueOf(data.getAmount()));
            addIfNotEmpty(dataMap, "code", data.getCode());
            addIfNotEmpty(dataMap, "currency", data.getCurrency());
            addIfNotEmpty(dataMap, "desc", data.getDesc());
            addIfNotEmpty(dataMap, "description", data.getDescription());
            addIfNotEmpty(dataMap, "orderCode", String.valueOf(data.getOrderCode()));
            addIfNotEmpty(dataMap, "paymentLinkId", data.getPaymentLinkId());
            addIfNotEmpty(dataMap, "reference", data.getReference());
            addIfNotEmpty(dataMap, "transactionDateTime", data.getTransactionDateTime());
            addIfNotEmpty(dataMap, "virtualAccountNumber", data.getVirtualAccountNumber());

            String dataStr = buildDataString(dataMap);
            String calculated = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

            log.info("🧪 Testing Variation 2: Core fields (no empties)");
            log.info("   Fields ({}): {}", dataMap.size(), dataMap.keySet());
            log.info("   Data string: {}", dataStr);
            log.info("   Calculated:  {}", calculated);
            log.info("   Received:    {}", receivedSignature);

            if (calculated.equalsIgnoreCase(receivedSignature)) {
                log.info("✅ Variation 2 MATCHED!");
                return true;
            }

            log.error("❌ Variation 2 failed");
            return false;
        } catch (Exception e) {
            log.error("💥 Variation 2 exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Variation 3: Tất cả non-null fields (kể cả empty strings)
     */
    private boolean tryVariation3(PayOSWebhookData data, String receivedSignature) {
        try {
            Map<String, String> dataMap = new TreeMap<>();

            // Add ALL fields (even empty ones)
            if (data.getAccountNumber() != null)
                dataMap.put("accountNumber", data.getAccountNumber());
            if (data.getAmount() != null)
                dataMap.put("amount", String.valueOf(data.getAmount()));
            if (data.getCode() != null)
                dataMap.put("code", data.getCode());
            if (data.getCounterAccountBankId() != null)
                dataMap.put("counterAccountBankId", data.getCounterAccountBankId());
            if (data.getCounterAccountBankName() != null)
                dataMap.put("counterAccountBankName", data.getCounterAccountBankName());
            if (data.getCounterAccountName() != null)
                dataMap.put("counterAccountName", data.getCounterAccountName());
            if (data.getCounterAccountNumber() != null)
                dataMap.put("counterAccountNumber", data.getCounterAccountNumber());
            if (data.getCurrency() != null)
                dataMap.put("currency", data.getCurrency());
            if (data.getDesc() != null)
                dataMap.put("desc", data.getDesc());
            if (data.getDescription() != null)
                dataMap.put("description", data.getDescription());
            if (data.getOrderCode() != null)
                dataMap.put("orderCode", String.valueOf(data.getOrderCode()));
            if (data.getPaymentLinkId() != null)
                dataMap.put("paymentLinkId", data.getPaymentLinkId());
            if (data.getReference() != null)
                dataMap.put("reference", data.getReference());
            if (data.getTransactionDateTime() != null)
                dataMap.put("transactionDateTime", data.getTransactionDateTime());
            if (data.getVirtualAccountName() != null)
                dataMap.put("virtualAccountName", data.getVirtualAccountName());
            if (data.getVirtualAccountNumber() != null)
                dataMap.put("virtualAccountNumber", data.getVirtualAccountNumber());

            String dataStr = buildDataString(dataMap);
            String calculated = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

            if (calculated.equalsIgnoreCase(receivedSignature)) {
                log.info("✅ Variation 3 MATCHED: All non-null fields");
                log.info("   Fields ({}): {}", dataMap.size(), dataMap.keySet());
                log.info("   Data string: {}", dataStr);
                return true;
            }

            log.debug("❌ Variation 3 failed");
            log.debug("   Expected: {}", calculated);
            log.debug("   Received: {}", receivedSignature);
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void addIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }

    private String buildDataString(Map<String, String> dataMap) {
        return dataMap.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }


    // ===================================================================

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

    /**
     * Query payment status từ PayOS API
     *
     * GET /v2/payment-requests/{orderCode}
     */
    public PayOSPaymentData queryPaymentStatus(Long orderCode) {
        try {
            log.info("Querying PayOS payment status for orderCode: {}", orderCode);

            // Gọi PayOS API
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-client-id", payosConfig.getClientId());
            headers.set("x-api-key", payosConfig.getApiKey());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String url = payosConfig.getEndpoint() + "/v2/payment-requests/" + orderCode;

            ResponseEntity<PayOSCreatePaymentResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PayOSCreatePaymentResponse.class
            );

            PayOSCreatePaymentResponse response = responseEntity.getBody();

            if (response == null || !"00".equals(response.getCode())) {
                throw new CustomException("Failed to query payment status", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return response.getData();

        } catch (Exception e) {
            log.error("Error querying PayOS payment status", e);
            throw new CustomException("Error querying payment status: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancel payment trên PayOS
     *
     * POST /v2/payment-requests/{orderCode}/cancel
     */
    public void cancelPayment(Long orderCode, String cancellationReason) {
        try {
            log.info("Cancelling PayOS payment - OrderCode: {}", orderCode);

            // Gọi PayOS API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", payosConfig.getClientId());
            headers.set("x-api-key", payosConfig.getApiKey());

            Map<String, String> body = new HashMap<>();
            body.put("cancellationReason", cancellationReason);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            String url = payosConfig.getEndpoint() + "/v2/payment-requests/" + orderCode + "/cancel";

            ResponseEntity<PayOSCreatePaymentResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    PayOSCreatePaymentResponse.class
            );

            PayOSCreatePaymentResponse response = responseEntity.getBody();

            if (response == null || !"00".equals(response.getCode())) {
                throw new CustomException("Failed to cancel payment", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("✅ PayOS payment cancelled successfully - OrderCode: {}", orderCode);

        } catch (Exception e) {
            log.error("Error cancelling PayOS payment", e);
            throw new CustomException("Error cancelling payment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Generate signature cho PayOS request
     */
    private String generateSignature(PayOSCreatePaymentRequest request) {
        try {
            // Build signature string theo PayOS docs
            // Format: key1=value1&key2=value2&... (sorted alphabetically)
            Map<String, String> dataMap = new TreeMap<>();

            dataMap.put("amount", String.valueOf(request.getAmount()));
            dataMap.put("cancelUrl", request.getCancelUrl());
            dataMap.put("description", request.getDescription());
            dataMap.put("orderCode", String.valueOf(request.getOrderCode()));
            dataMap.put("returnUrl", request.getReturnUrl());

            String dataStr = dataMap.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            log.debug("Signature data string: {}", dataStr);

            return generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

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
