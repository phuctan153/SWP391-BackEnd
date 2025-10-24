package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.config.MomoConfigProperties;
import com.example.ev_rental_backend.dto.payment.*;
import com.example.ev_rental_backend.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentService {

    private final MomoConfigProperties momoConfig;
    private final RestTemplate restTemplate;

    /**
     * Tạo payment request với MoMo
     *
     * @param transactionId ID transaction trong hệ thống
     * @param amount Số tiền thanh toán
     * @param orderInfo Mô tả đơn hàng
     * @return Thông tin payment (payUrl, qrCode, etc.)
     */
    public MomoPaymentInfoDto createPayment(Long transactionId, Long amount, String orderInfo) {
        try {
            // 1. Tạo các tham số
            String orderId = generateOrderId(transactionId);
            String requestId = UUID.randomUUID().toString();
            String extraData = ""; // Có thể để trống hoặc truyền thêm data

            log.info("Creating MoMo payment - OrderId: {}, Amount: {}", orderId, amount);

            // 2. Tạo raw signature
            String rawSignature = buildRawSignature(
                    momoConfig.getAccessKey(),
                    amount,
                    extraData,
                    momoConfig.getIpnUrl(),
                    orderId,
                    orderInfo,
                    momoConfig.getPartnerCode(),
                    momoConfig.getRedirectUrl(),
                    requestId,
                    momoConfig.getRequestType()
            );

            // 3. Generate HMAC SHA256 signature
            String signature = generateHMACSHA256(rawSignature, momoConfig.getSecretKey());

            // 4. Tạo request body
            MomoCreatePaymentRequest request = MomoCreatePaymentRequest.builder()
                    .partnerCode(momoConfig.getPartnerCode())
                    .accessKey(momoConfig.getAccessKey())
                    .requestId(requestId)
                    .amount(amount)
                    .orderId(orderId)
                    .orderInfo(orderInfo)
                    .redirectUrl(momoConfig.getRedirectUrl())
                    .ipnUrl(momoConfig.getIpnUrl())
                    .extraData(extraData)
                    .requestType(momoConfig.getRequestType())
                    .signature(signature)
                    .lang("vi")
                    .build();

            log.debug("MoMo request: {}", request);

            // 5. Gọi MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<MomoCreatePaymentRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<MomoCreatePaymentResponse> responseEntity = restTemplate.exchange(
                    momoConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    MomoCreatePaymentResponse.class
            );

            MomoCreatePaymentResponse response = responseEntity.getBody();

            // 6. Kiểm tra response
            if (response == null) {
                throw new CustomException("MoMo response is null", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            log.info("MoMo response - ResultCode: {}, Message: {}",
                    response.getResultCode(), response.getMessage());

            // 7. Parse response
            if (response.getResultCode() == 0) {
                // Success
                return MomoPaymentInfoDto.builder()
                        .transactionId(transactionId)
                        .orderId(orderId)
                        .payUrl(response.getPayUrl())
                        .qrCodeUrl(response.getQrCodeUrl())
                        .deeplink(response.getDeeplink())
                        .amount(amount)
                        .message("Payment created successfully")
                        .resultCode(0)
                        .build();
            } else {
                // Error
                log.error("MoMo payment creation failed - Code: {}, Message: {}",
                        response.getResultCode(), response.getMessage());

                return MomoPaymentInfoDto.builder()
                        .transactionId(transactionId)
                        .message("Failed to create payment: " + response.getMessage())
                        .resultCode(response.getResultCode())
                        .build();
            }

        } catch (Exception e) {
            log.error("Error creating MoMo payment", e);
            throw new CustomException("Error creating MoMo payment: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xác thực signature từ MoMo IPN
     *
     * @param ipnRequest IPN request từ MoMo
     * @return true nếu signature hợp lệ
     */
    public boolean verifyIPNSignature(MomoIPNRequest ipnRequest) {
        try {
            // Build raw signature từ IPN data
            String rawSignature = String.format(
                    "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
                    momoConfig.getAccessKey(),
                    ipnRequest.getAmount(),
                    ipnRequest.getExtraData(),
                    ipnRequest.getMessage(),
                    ipnRequest.getOrderId(),
                    ipnRequest.getOrderInfo(),
                    ipnRequest.getOrderType(),
                    ipnRequest.getPartnerCode(),
                    ipnRequest.getPayType(),
                    ipnRequest.getRequestId(),
                    ipnRequest.getResponseTime(),
                    ipnRequest.getResultCode(),
                    ipnRequest.getTransId()
            );

            // Generate signature
            String calculatedSignature = generateHMACSHA256(rawSignature, momoConfig.getSecretKey());

            // So sánh
            boolean isValid = calculatedSignature.equals(ipnRequest.getSignature());

            if (!isValid) {
                log.error("Invalid MoMo IPN signature - Expected: {}, Received: {}",
                        calculatedSignature, ipnRequest.getSignature());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying MoMo IPN signature", e);
            return false;
        }
    }

    /**
     * Tạo response cho MoMo IPN
     */
    public MomoIPNResponse createIPNResponse(MomoIPNRequest ipnRequest, boolean success) {
        return MomoIPNResponse.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .orderId(ipnRequest.getOrderId())
                .requestId(ipnRequest.getRequestId())
                .amount(ipnRequest.getAmount())
                .responseTime(System.currentTimeMillis())
                .message(success ? "Success" : "Failed")
                .resultCode(success ? 0 : 1)
                .build();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Tạo Order ID unique
     * Format: EV_{transactionId}_{timestamp}
     */
    private String generateOrderId(Long transactionId) {
        return String.format("EV_%d_%d", transactionId, System.currentTimeMillis());
    }

    /**
     * Build raw signature string theo thứ tự alphabet
     */
    private String buildRawSignature(String accessKey, Long amount, String extraData,
                                     String ipnUrl, String orderId, String orderInfo,
                                     String partnerCode, String redirectUrl,
                                     String requestId, String requestType) {
        return String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, extraData, ipnUrl, orderId, orderInfo,
                partnerCode, redirectUrl, requestId, requestType
        );
    }

    /**
     * Generate HMAC SHA256 signature
     *
     * @param data Raw data
     * @param secretKey Secret key
     * @return Hex string signature
     */
    private String generateHMACSHA256(String data, String secretKey) {
        try {
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
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

    /**
     * Parse transaction ID từ Order ID
     * OrderId format: EV_{transactionId}_{timestamp}
     */
    public Long parseTransactionIdFromOrderId(String orderId) {
        try {
            String[] parts = orderId.split("_");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
            throw new IllegalArgumentException("Invalid orderId format");
        } catch (Exception e) {
            log.error("Error parsing transactionId from orderId: {}", orderId, e);
            throw new CustomException("Invalid order ID format", HttpStatus.BAD_REQUEST);
        }
    }
}
