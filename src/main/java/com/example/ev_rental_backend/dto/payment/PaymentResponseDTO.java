package com.example.ev_rental_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long transactionId;
    private Long invoiceId;
    private Long bookingId;
    private String invoiceType; // DEPOSIT, FINAL

    private Double amount;
    private String paymentMethod;
    private String status;

    // Momo payment info
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}