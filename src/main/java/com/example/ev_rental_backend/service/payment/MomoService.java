package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.client.MomoApi;
import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class MomoService {

    private final MomoApi momoApi;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentService paymentService;

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

    // ⭐ Constructor injection với @Lazy
    public MomoService(
            MomoApi momoApi,
            BookingRepository bookingRepository,
            InvoiceRepository invoiceRepository,
            @Lazy PaymentService paymentService) { // ⭐ THÊM @Lazy
        this.momoApi = momoApi;
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentService = paymentService;
    }

    /**
     * Tạo yêu cầu thanh toán Momo cho booking
     */
    @Transactional
    public CreateMomoResponse createMomoPayment(Long bookingId, Long invoiceId) {
        // 1. Validate booking & invoice
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy booking: " + bookingId));

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy invoice: " + invoiceId));

        // 2. Kiểm tra invoice chưa thanh toán
        if (invoice.getStatus() == Invoice.Status.COMPLETED) {
            throw new CustomException("Invoice đã được thanh toán");
        }

        // 3. Tạo IDs
        String orderId = "BK" + bookingId + "_INV" + invoiceId + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toan " + invoice.getType().name() + " - Booking #" + bookingId;

        // 4. Encode extraData (bookingId + invoiceId)
        String extraData = Base64.getEncoder().encodeToString(
                ("bookingId=" + bookingId + "|invoiceId=" + invoiceId).getBytes(StandardCharsets.UTF_8)
        );

        long amount = invoice.getTotalAmount().longValue();

        // 5. Tạo signature
        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                ACCESS_KEY, amount, extraData, IPN_URL, orderId, orderInfo, PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE
        );

        String signature;
        try {
            signature = signHmacSHA256(rawSignature, SECRET_KEY);
            log.info("📝 Momo signature created for order: {}", orderId);
        } catch (Exception e) {
            log.error("❌ Error signing HMAC SHA256: {}", e.getMessage());
            throw new CustomException("Lỗi tạo chữ ký thanh toán Momo");
        }

        // 6. Tạo request Momo
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

        // 7. Gọi Momo API
        CreateMomoResponse response;
        try {
            response = momoApi.createMomo(request);
            log.info("✅ Momo payment created: orderId={}, payUrl={}", orderId, response.getPayUrl());
        } catch (Exception e) {
            log.error("❌ Error calling Momo API: {}", e.getMessage(), e);
            throw new CustomException("Không thể kết nối đến Momo: " + e.getMessage());
        }

        // 8. Kiểm tra response
        if (response.getResultCode() != 0) {
            log.error("❌ Momo API error: code={}, message={}",
                    response.getResultCode(), response.getMessage());
            throw new CustomException("Momo trả về lỗi: " + response.getMessage());
        }

        return response;
    }

    /**
     * Xử lý IPN callback từ Momo
     */
    @Transactional
    public void handleMomoIPN(MomoIPNRequest request) throws Exception {
        log.info("🔔 Received Momo IPN: orderId={}, resultCode={}, transId={}",
                request.getOrderId(), request.getResultCode(), request.getTransId());

        // 1. Validate signature
        String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
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
            log.error("❌ Invalid Momo signature!\nExpected: {}\nReceived: {}",
                    computedSignature, request.getSignature());
            throw new CustomException("Chữ ký Momo không hợp lệ");
        }

        log.info("✅ Momo signature validated successfully");

        // 2. Giải mã extraData để lấy bookingId & invoiceId
        String extraData = new String(
                Base64.getDecoder().decode(request.getExtraData()),
                StandardCharsets.UTF_8
        );

        log.info("📦 ExtraData decoded: {}", extraData);

        String[] parts = extraData.split("\\|");
        Long bookingId = null;
        Long invoiceId = null;

        for (String part : parts) {
            if (part.startsWith("bookingId=")) {
                bookingId = Long.valueOf(part.replace("bookingId=", ""));
            }
            if (part.startsWith("invoiceId=")) {
                invoiceId = Long.valueOf(part.replace("invoiceId=", ""));
            }
        }

        if (bookingId == null || invoiceId == null) {
            log.error("❌ Cannot extract bookingId or invoiceId from extraData: {}", extraData);
            throw new CustomException("Dữ liệu callback không hợp lệ");
        }

        log.info("🔍 Extracted: bookingId={}, invoiceId={}", bookingId, invoiceId);

        // 3. Xử lý thanh toán dựa trên resultCode
        if (request.getResultCode() == 0) {
            // Thanh toán thành công
            log.info("✅ Payment SUCCESS for booking {} - invoice {}", bookingId, invoiceId);
            paymentService.handlePaymentSuccess(
                    invoiceId,
                    String.valueOf(request.getTransId()),
                    request.getAmount().doubleValue()
            );
        } else {
            // Thanh toán thất bại
            log.warn("❌ Payment FAILED for booking {} - invoice {}: {}",
                    bookingId, invoiceId, request.getMessage());
            paymentService.handlePaymentFailed(invoiceId, request.getMessage());
        }
    }

    /**
     * Ký HMAC SHA256
     */
    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
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
}
