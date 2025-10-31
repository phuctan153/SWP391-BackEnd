package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import com.example.ev_rental_backend.dto.payment.MomoIPNResponse;
import com.example.ev_rental_backend.dto.payment.MomoPaymentInfoDto;
import com.example.ev_rental_backend.dto.payment.PaymentRequestDto;
import com.example.ev_rental_backend.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class MomoPaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo payment MoMo cho invoice
     *
     * POST /api/payments/invoice/{invoiceId}/momo
     *
     * Flow:
     * 1. Client gọi API này
     * 2. Backend tạo payment transaction (PENDING)
     * 3. Backend gọi MoMo API
     * 4. Backend trả về payUrl cho client
     * 5. Client redirect user đến payUrl
     * 6. User thanh toán trên MoMo
     * 7. MoMo gọi IPN về backend
     * 8. Backend cập nhật transaction
     */
    @PostMapping("/invoice/{invoiceId}/momo")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<MomoPaymentInfoDto>> createMomoPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequestDto requestDto) {

        log.info("Creating MoMo payment for invoice {}, amount: {}",
                invoiceId, requestDto.getAmount());

        MomoPaymentInfoDto paymentInfo = paymentService.createMomoPayment(invoiceId, requestDto);

        return ResponseEntity.ok(ApiResponse.<MomoPaymentInfoDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(paymentInfo)
                .build());
    }

    /**
     * IPN (Instant Payment Notification) từ MoMo
     *
     * POST /api/payments/momo/ipn
     *
     * MoMo sẽ gọi API này để thông báo kết quả thanh toán
     *
     * QUAN TRỌNG:
     * - Endpoint này phải public (không cần authentication)
     * - Phải verify signature từ MoMo
     * - Phải trả về response đúng format cho MoMo
     */
    @PostMapping("/momo/ipn")
    public ResponseEntity<MomoIPNResponse> handleMomoIPN(@RequestBody MomoIPNRequest ipnRequest) {

        log.info("Received MoMo IPN - OrderId: {}, ResultCode: {}, TransId: {}",
                ipnRequest.getOrderId(), ipnRequest.getResultCode(), ipnRequest.getTransId());

        log.debug("MoMo IPN full data: {}", ipnRequest);

        try {
            // Xử lý IPN
            MomoIPNResponse ipnResponse = paymentService.handleMomoIPN(ipnRequest);

            log.info("MoMo IPN processed successfully - OrderId: {}", ipnRequest.getOrderId());

            return ResponseEntity.ok(ipnResponse);

        } catch (Exception e) {
            log.error("Error processing MoMo IPN - OrderId: {}", ipnRequest.getOrderId(), e);

            // Vẫn phải trả về response cho MoMo (resultCode = 1 để báo lỗi)
            MomoIPNResponse errorResponse = MomoIPNResponse.builder()
                    .partnerCode(ipnRequest.getPartnerCode())
                    .orderId(ipnRequest.getOrderId())
                    .requestId(ipnRequest.getRequestId())
                    .amount(ipnRequest.getAmount())
                    .responseTime(System.currentTimeMillis())
                    .message("Error: " + e.getMessage())
                    .resultCode(1)
                    .build();

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Redirect callback từ MoMo (khi user thanh toán xong trên web)
     *
     * GET /api/payments/momo/callback
     *
     * User sẽ được redirect về đây sau khi thanh toán
     * Thường sẽ redirect tiếp về frontend
     */
    @GetMapping("/momo/callback")
    public ResponseEntity<String> handleMomoCallback(
            @RequestParam String partnerCode,
            @RequestParam String orderId,
            @RequestParam String requestId,
            @RequestParam Long amount,
            @RequestParam String orderInfo,
            @RequestParam String orderType,
            @RequestParam Long transId,
            @RequestParam Integer resultCode,
            @RequestParam String message,
            @RequestParam String payType,
            @RequestParam Long responseTime,
            @RequestParam String extraData,
            @RequestParam String signature) {

        log.info("MoMo callback - OrderId: {}, ResultCode: {}", orderId, resultCode);

        // Có thể redirect về frontend với thông tin thanh toán
        String frontendUrl = "https://swp-391-frontend-mu.vercel.app/payment-result";
        String redirectUrl = String.format(
                "%s?orderId=%s&resultCode=%d&message=%s",
                frontendUrl, orderId, resultCode, message
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .body("Redirecting...");
    }

    /**
     * Check trạng thái thanh toán MoMo
     *
     * GET /api/payments/momo/status/{orderId}
     */
    @GetMapping("/momo/status/{orderId}")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> checkMomoPaymentStatus(@PathVariable String orderId) {

        log.info("Checking MoMo payment status for orderId: {}", orderId);

        // Có thể gọi MoMo Query API để check status
        // Hoặc query từ database

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data("Order status check not implemented yet")
                .build());
    }
}
