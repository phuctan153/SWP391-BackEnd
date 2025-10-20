package com.example.ev_rental_backend.dto.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class ReturnRequestDto {

    @NotNull(message = "Battery level is required")
    @Min(value = 0, message = "Battery level must be at least 0")
    @Max(value = 100, message = "Battery level must be at most 100")
    private Double batteryLevel;

    @NotNull(message = "Mileage is required")
    @Positive(message = "Mileage must be positive")
    private Double mileage;

    private Boolean hasDamage;

    private String damageDescription;

    private Double damageFee;

    private String notes;
}
