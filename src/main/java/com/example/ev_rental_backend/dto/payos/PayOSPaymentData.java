package com.example.ev_rental_backend.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSPaymentData {
    @JsonProperty("bin")
    private String bin;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("description")
    private String description;

    @JsonProperty("orderCode")
    private Long orderCode;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("paymentLinkId")
    private String paymentLinkId;

    @JsonProperty("status")
    private String status; // PENDING, PROCESSING, PAID, CANCELLED

    @JsonProperty("checkoutUrl")
    private String checkoutUrl; // URL để redirect user

    @JsonProperty("qrCode")
    private String qrCode; // QR code URL
}
