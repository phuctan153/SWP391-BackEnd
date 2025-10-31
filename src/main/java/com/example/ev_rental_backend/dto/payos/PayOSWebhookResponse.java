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
public class PayOSWebhookResponse {
    @JsonProperty("error")
    private Integer error; // 0 = success, khác 0 = error

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Object data;
}
