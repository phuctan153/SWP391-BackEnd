package com.example.ev_rental_backend.dto.payment;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    private Long transactionId;
    private Long invoiceId;
    private BigDecimal amount;
    private PaymentTransaction.Status status;
    private PaymentTransaction.TransactionType transactionType;
    private LocalDateTime transactionTime;
    private String message;
}