package com.example.ev_rental_backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentInitRequestDTO {

    @NotNull(message = "Invoice ID không được để trống")
    @Positive(message = "Invoice ID phải là số dương")
    private Long invoiceId;

    private String paymentMethod;
}
