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
public class PaymentTransactionResponseDto {

    private Long transactionId;

    private Long walletId;          // Lấy từ wallet.getWalletId()
    private Long invoiceId;         // Lấy từ invoice.getInvoiceId()

    private LocalDateTime transactionTime;

    private BigDecimal amount;

    private PaymentTransaction.Status status;

    private PaymentTransaction.TransactionType transactionType;

    private Long orderCode;

    private String paymentLinkId;

    private String referenceCode;

    private String notes;
}
