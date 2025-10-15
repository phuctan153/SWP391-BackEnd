package com.example.ev_rental_backend.dto.payment;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomoRequest {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private String orderInfo;
    private Long amount;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType;
    private String extraData;
    private String signature;
    private String lang;
}
