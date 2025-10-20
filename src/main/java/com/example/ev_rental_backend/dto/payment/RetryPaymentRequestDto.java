package com.example.ev_rental_backend.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryPaymentRequestDto {
    @NotNull(message = "Transaction ID is required")
    private Long transactionId;
}
