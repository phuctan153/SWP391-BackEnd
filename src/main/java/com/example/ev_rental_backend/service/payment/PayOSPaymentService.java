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

    public boolean verifyWebhookSignature(PayOSWebhookRequest webhookData) {
        try {
//            // Build data string for signature theo thứ tự alphabet
//            PayOSWebhookData data = webhookData.getData();
//
//            // 🔑 QUAN TRỌNG: PayOS yêu cầu sort keys theo alphabet
//            Map<String, String> dataMap = new TreeMap<>(); // TreeMap tự động sort
//
//            dataMap.put("amount", String.valueOf(data.getAmount()));
//            dataMap.put("code", data.getCode());
//            dataMap.put("desc", data.getDesc());
//            dataMap.put("orderCode", String.valueOf(data.getOrderCode()));
//
//            // Optional fields (chỉ thêm nếu có giá trị)
//            if (data.getReference() != null) {
//                dataMap.put("reference", data.getReference());
//            }
//            if (data.getTransactionDateTime() != null) {
//                dataMap.put("transactionDateTime", data.getTransactionDateTime());
//            }
//
//            // Build data string: key1=value1&key2=value2&...
//            String dataStr = dataMap.entrySet().stream()
//                    .map(entry -> entry.getKey() + "=" + entry.getValue())
//                    .collect(Collectors.joining("&"));
//
//            log.debug("Webhook data string for signature: {}", dataStr);
//
//            // Generate signature
//            String calculatedSignature = generateHMACSHA256(dataStr, payosConfig.getChecksumKey());

            ObjectMapper mapper = new ObjectMapper();
            // Giữ nguyên: loại bỏ null fields
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            // --- BẮT ĐẦU PHẦN CHỈNH SỬA QUAN TRỌNG ---

            // 1. Chuyển DTO PayOSWebhookData sang Map<String, Object>
            Map<String, Object> dataMap = mapper.convertValue(webhookData.getData(), new TypeReference<Map<String, Object>>() {});

            // 2. Sắp xếp Map theo key (alphabetical order)
            // Dùng TreeMap để đảm bảo key được sắp xếp
            Map<String, Object> sortedDataMap = new TreeMap<>(dataMap);

            // 3. Serialize Map đã sắp xếp thành chuỗi JSON COMPACT (Không khoảng trắng, không xuống dòng)
            // Đây là bước quan trọng nhất để khớp với chuỗi PayOS dùng để ký.
            String rawData = mapper.writeValueAsString(sortedDataMap);

            log.debug("Raw JSON used for signature (sorted and compact): {}", rawData);

            // --- KẾT THÚC PHẦN CHỈNH SỬA QUAN TRỌNG ---

            // Giữ nguyên logic tính toán signature
            String calculatedSignature = generateHMACSHA256(rawData, payosConfig.getChecksumKey());

            // Compare
            boolean isValid = calculatedSignature.equals(webhookData.getSignature());

            if (!isValid) {
                log.error("Invalid PayOS webhook signature - Expected: {}, Received: {}",
                        calculatedSignature, webhookData.getSignature());
                log.error("Data string used: {}", rawData);
            } else {
                log.info("✅ PayOS webhook signature verified successfully");
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
