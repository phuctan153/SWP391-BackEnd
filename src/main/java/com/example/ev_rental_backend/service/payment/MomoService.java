package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.client.MomoApi;
import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    private final MomoApi momoApi;

    @Value(value = "${momo.partner-code}")
    private String PARTNER_CODE;

    @Value(value = "${momo.access-key}")
    private String ACCESS_KEY;

    @Value(value = "${momo.secret-key}")
    private String SECRET_KEY;

    @Value(value = "${momo.return-url}")
    private String REDIRECT_URL;

    @Value(value = "${momo.ipn-url}")
    private String IPN_URL;

    @Value(value = "${momo.request-type}")
    private String REQUEST_TYPE;

    public CreateMomoResponse createMomo() {

        String orderId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String requestId = UUID.randomUUID().toString();
        String extraData = "";
        long amount = 2000;

        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount, extraData, IPN_URL, orderId, orderInfo, PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE
        );

        String signature = "";
        try {
            signature = signHmacSHA256(rawSignature, SECRET_KEY);
        } catch (Exception e) {
            log.error("Error signing HMAC SHA256: {}", e.getMessage());
        }

        if(signature.isBlank()){
            throw new RuntimeException("Error generating signature for Momo payment");
        }

        CreateMomoRequest request = CreateMomoRequest.builder()
                .partnerCode(PARTNER_CODE)
                .requestType(REQUEST_TYPE)
                .ipnUrl(IPN_URL)
                .redirectUrl(REDIRECT_URL)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .requestId(requestId)
                .extraData(extraData)
                .amount(amount)
                .signature(signature)
                .lang("vi")
                .build();

        return momoApi.createMomo(request);
    }

    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void handleMomoIPN(MomoIPNRequest request) throws Exception {
        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%s",
                ACCESS_KEY,
                request.getAmount(),
                request.getExtraData(),
                request.getMessage(),
                request.getOrderId(),
                request.getOrderInfo(),
                request.getOrderType(),
                PARTNER_CODE,
                request.getPayType(),
                request.getRequestId(),
                request.getResponseTime(),
                request.getResultCode(),
                request.getTransId()
        );

        String computedSignature = signHmacSHA256(rawSignature, SECRET_KEY);

        if (!computedSignature.equals(request.getSignature())) {
            log.error("❌ Invalid Momo signature!\nExpected: {}\nReceived: {}", computedSignature, request.getSignature());
            throw new RuntimeException("Invalid Momo signature!");
        }

        if (request.getResultCode() == 0) {
            log.info("✅ Thanh toán thành công cho orderId: {}", request.getOrderId());
            // TODO: cập nhật trạng thái booking
        } else {
            log.warn("❌ Thanh toán thất bại cho orderId: {} - message: {}", request.getOrderId(), request.getMessage());
        }
    }

}
