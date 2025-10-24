package com.example.ev_rental_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoPaymentInfoDto {

    private Long transactionId; // Transaction ID trong hệ thống
    private String orderId; // Order ID của MoMo
    private String payUrl; // URL để thanh toán
    private String qrCodeUrl; // QR code
    private String deeplink; // Deep link
    private Long amount;
    private String message;
    private Integer resultCode;
}
