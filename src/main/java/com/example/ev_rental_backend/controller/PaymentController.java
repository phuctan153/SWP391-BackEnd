package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.payment.*;
import com.example.ev_rental_backend.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 7.3. Payment Processing

    /**
     * POST /api/payments/invoice/{invoiceId}/cash - Thanh toán bằng tiền mặt
     */
    @PostMapping("/invoice/{invoiceId}/cash")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> payByCash(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto payment = paymentService.payByCash(invoiceId, requestDto);
        return ResponseEntity.ok(ApiResponse.<PaymentResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(payment)
                .build());
    }

    /**
     * POST /api/payments/invoice/{invoiceId}/wallet - Thanh toán bằng ví (BR-30)
     */
    @PostMapping("/invoice/{invoiceId}/wallet")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> payByWallet(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequestDto requestDto) {
        PaymentResponseDto payment = paymentService.payByWallet(invoiceId, requestDto);
        return ResponseEntity.ok(ApiResponse.<PaymentResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(payment)
                .build());
    }

    /**
     * POST /api/payments/invoice/{invoiceId}/retry - Thử lại giao dịch thất bại (BR-29)
     */
    @PostMapping("/invoice/{invoiceId}/retry")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> retryPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody RetryPaymentRequestDto requestDto) {
        PaymentResponseDto payment = paymentService.retryPayment(invoiceId, requestDto);
        return ResponseEntity.ok(ApiResponse.<PaymentResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(payment)
                .build());
    }

    /**
     * GET /api/payments/transactions/{transactionId} - Chi tiết giao dịch
     */
    @GetMapping("/transactions/{transactionId}")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<TransactionResponseDto>> getTransactionById(
            @PathVariable Long transactionId) {
        TransactionResponseDto transaction = paymentService.getTransactionById(transactionId);
        return ResponseEntity.ok(ApiResponse.<TransactionResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(transaction)
                .build());
    }

}
