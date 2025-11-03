package com.example.ev_rental_backend.dto.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSPaymentInfoDto {
    private Long transactionId; // Transaction ID trong hệ thống
    private Long orderCode; // Order code PayOS
    private String checkoutUrl; // URL thanh toán
    private String qrCode; // QR code
    private Integer amount;
    private String status;
    private String message;
}
