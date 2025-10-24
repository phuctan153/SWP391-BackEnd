package com.example.ev_rental_backend.dto.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupRequestDto {

    private Long staffId;

    @NotBlank(message = "Identity document number is required")
    private String identityDocumentNumber;

    private String notes;
}
