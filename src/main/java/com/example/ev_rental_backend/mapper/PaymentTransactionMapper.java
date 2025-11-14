package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.payment.PaymentTransactionResponseDto;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PaymentTransactionResponseDto toDto(PaymentTransaction tx) {
        if (tx == null) return null;

        return PaymentTransactionResponseDto.builder()
                .transactionId(tx.getTransactionId())
                .walletId(tx.getWallet() != null ? tx.getWallet().getWalletId() : null)
                .invoiceId(tx.getInvoice() != null ? tx.getInvoice().getInvoiceId() : null)
                .transactionTime(tx.getTransactionTime())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .transactionType(tx.getTransactionType())
                .orderCode(tx.getOrderCode())
                .paymentLinkId(tx.getPaymentLinkId())
                .referenceCode(tx.getReferenceCode())
                .notes(tx.getNotes())
                .build();
    }
}
