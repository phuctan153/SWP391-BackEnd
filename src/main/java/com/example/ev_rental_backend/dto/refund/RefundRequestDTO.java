package com.example.ev_rental_backend.dto.refund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDTO {
    @NotNull(message = "Số tiền hoàn không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền hoàn phải lớn hơn 0")
    private Double amount;

    private String reason; // Ví dụ: "Hoàn phần dư cọc"
}
