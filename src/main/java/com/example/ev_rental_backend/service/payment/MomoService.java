package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.payment.MomoPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    @Value("${momo.partner-code:MOMO_PARTNER}")
    private String partnerCode;

    @Value("${momo.access-key:ACCESS_KEY}")
    private String accessKey;

    @Value("${momo.secret-key:SECRET_KEY}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String momoEndpoint;

    @Value("${momo.redirect-url:http://localhost:8080/api/payments/momo/callback}")
    private String redirectUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/payments/momo/callback}")
    private String ipnUrl;

    private final RestTemplate restTemplate;

    /**
     * Tạo payment request với MoMo
     */
    public MomoPaymentResponseDto createPayment(Long transactionId, Double amount, String orderInfo) {
        try {
            String orderId = "EV_" + transactionId + "_" + System.currentTimeMillis();
            String requestId = UUID.randomUUID().toString();

            // Tạo raw signature
            String rawSignature = String.format(
                    "accessKey=%s&amount=%.0f&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    accessKey, amount, "", ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, "captureWallet"
            );

            // Generate signature
            String signature = generateHmacSHA256(rawSignature, secretKey);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount.longValue());
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", "");
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");

            // Gọi MoMo API
            Map<String, Object> response = restTemplate.postForObject(
                    momoEndpoint,
                    requestBody,
                    Map.class
            );

            // Parse response
            if (response != null && "0".equals(String.valueOf(response.get("resultCode")))) {
                return MomoPaymentResponseDto.builder()
                        .transactionId(transactionId)
                        .payUrl((String) response.get("payUrl"))
                        .qrCodeUrl((String) response.get("qrCodeUrl"))
                        .orderId(orderId)
                        .amount(amount)
                        .message("Payment created successfully")
                        .build();
            } else {
                String errorMessage = response != null ?
                        (String) response.get("message") : "Unknown error";
                log.error("MoMo payment creation failed: {}", errorMessage);

                return MomoPaymentResponseDto.builder()
                        .transactionId(transactionId)
                        .message("Failed to create MoMo payment: " + errorMessage)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            return MomoPaymentResponseDto.builder()
                    .transactionId(transactionId)
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String generateHmacSHA256(String data, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            sha256_HMAC.init(secret_key);

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("Error generating signature", e);
            return "";
        }
    }

    /**
     * Verify MoMo callback signature
     */
    public boolean verifySignature(Map<String, String> params, String receivedSignature) {
        try {
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                    params.get("accessKey"), params.get("amount"), params.get("extraData"),
                    params.get("message"), params.get("orderId"), params.get("orderInfo"),
                    params.get("orderType"), params.get("partnerCode"), params.get("payType"),
                    params.get("requestId"), params.get("responseTime"), params.get("resultCode"),
                    params.get("transId")
            );

            String calculatedSignature = generateHmacSHA256(rawSignature, secretKey);
            return calculatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

}
