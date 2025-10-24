package com.example.ev_rental_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoPaymentResponseDto {

    private Long transactionId;
    private String payUrl;
    private String qrCodeUrl;
    private String orderId;
    private Long amount;
    private String message;
}
