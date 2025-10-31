package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.payment.PaymentRequestDto;
import com.example.ev_rental_backend.dto.payos.PayOSPaymentInfoDto;
import com.example.ev_rental_backend.dto.payos.PayOSWebhookRequest;
import com.example.ev_rental_backend.dto.payos.PayOSWebhookResponse;
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
public class PayOSPaymentController {

    private final PaymentService paymentService;

    /**
     * Tạo payment PayOS cho invoice
     *
     * POST /api/payments/invoice/{invoiceId}/payos
     *
     * Flow:
     * 1. Client gọi API này
     * 2. Backend tạo payment transaction (PENDING)
     * 3. Backend gọi PayOS API
     * 4. Backend trả về checkoutUrl cho client
     * 5. Client redirect user đến checkoutUrl
     * 6. User thanh toán trên PayOS
     * 7. PayOS gọi webhook về backend
     * 8. Backend cập nhật transaction
     */
    @PostMapping("/invoice/{invoiceId}/payos")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<PayOSPaymentInfoDto>> createPayOSPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequestDto requestDto) {

        log.info("Creating PayOS payment for invoice {}, amount: {}",
                invoiceId, requestDto.getAmount());

        PayOSPaymentInfoDto paymentInfo = paymentService.createPayOSPayment(invoiceId, requestDto);

        return ResponseEntity.ok(ApiResponse.<PayOSPaymentInfoDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(paymentInfo)
                .build());
    }

    /**
     * Webhook từ PayOS
     *
     * POST /api/payments/payos/webhook
     *
     * PayOS sẽ gọi API này để thông báo kết quả thanh toán
     *
     * QUAN TRỌNG:
     * - Endpoint này phải public (không cần authentication)
     * - Phải verify signature từ PayOS
     * - Phải trả về response đúng format cho PayOS
     */
    @PostMapping("/payos/webhook")
    public ResponseEntity<PayOSWebhookResponse> handlePayOSWebhook(
            @RequestBody PayOSWebhookRequest webhookRequest) {

        log.info("Received PayOS webhook - OrderCode: {}, Code: {}",
                webhookRequest.getData().getOrderCode(),
                webhookRequest.getData().getCode());

        log.debug("PayOS webhook full data: {}", webhookRequest);

        try {
            // Xử lý webhook
            PayOSWebhookResponse response = paymentService.handlePayOSWebhook(webhookRequest);

            log.info("PayOS webhook processed successfully - OrderCode: {}",
                    webhookRequest.getData().getOrderCode());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing PayOS webhook - OrderCode: {}",
                    webhookRequest.getData().getOrderCode(), e);

            // Vẫn phải trả về response cho PayOS
            PayOSWebhookResponse errorResponse = PayOSWebhookResponse.builder()
                    .error(1)
                    .message("Error: " + e.getMessage())
                    .data(null)
                    .build();

            return ResponseEntity.ok(errorResponse);
        }
    }
}
