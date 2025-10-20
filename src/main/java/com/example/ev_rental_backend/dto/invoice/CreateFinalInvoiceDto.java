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
public class CreateFinalInvoiceDto {
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    private String notes;
}
