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
public class PayOSWebhookRequest {

    @JsonProperty("code")
    private String code; // "00" = success

    @JsonProperty("desc")
    private String desc;

    @JsonProperty("data")
    private PayOSWebhookData data;

    @JsonProperty("signature")
    private String signature; // Verify signature
}
