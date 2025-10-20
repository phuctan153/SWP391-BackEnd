package com.example.ev_rental_backend.dto.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepositInvoiceDto {
    @NotNull(message = "Deposit amount is required")
    @Positive(message = "Deposit amount must be positive")
    private Double depositAmount;

    private String notes;

}
