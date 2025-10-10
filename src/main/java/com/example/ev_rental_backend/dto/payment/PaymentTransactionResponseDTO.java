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
public class PaymentTransactionResponseDTO {
    private Long transactionId;
    private Long paymentId;
    private String providerRef;
    private LocalDateTime transactionTime;
    private Double amount;
    private String status;
}