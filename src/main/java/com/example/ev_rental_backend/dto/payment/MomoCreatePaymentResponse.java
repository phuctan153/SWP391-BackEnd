package com.example.ev_rental_backend.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoCreatePaymentResponse {

    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private Long responseTime;
    private String message;
    private Integer resultCode; // 0: Success, khác 0: Error
    private String payUrl; // URL để redirect user
    private String qrCodeUrl; // QR code để quét
    private String deeplink; // Deep link để mở app MoMo

    @JsonProperty("applink")
    private String appLink;
}
