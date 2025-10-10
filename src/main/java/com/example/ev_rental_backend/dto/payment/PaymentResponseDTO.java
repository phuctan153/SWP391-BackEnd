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
    private Long paymentId;
    private Long invoiceId;
    private String method;
    private String status;
    private Double amount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}