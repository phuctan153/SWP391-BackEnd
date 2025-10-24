package com.example.ev_rental_backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoCallbackDto {

    private Long transactionId;
    private String orderId;
    private Integer resultCode;
    private String message;
    private Double amount;
    private String signature;
}
