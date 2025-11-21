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
public class PayOSCreatePaymentResponse {

    @JsonProperty("code")
    private String code; // "00" = success

    @JsonProperty("desc")
    private String desc; // Mô tả kết quả

    @JsonProperty("data")
    private PayOSPaymentData data; // Dữ liệu payment
}
