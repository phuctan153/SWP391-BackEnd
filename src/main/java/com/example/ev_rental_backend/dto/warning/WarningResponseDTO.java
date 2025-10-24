package com.example.ev_rental_backend.dto.warning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarningResponseDTO {
    private String renterEmail;
    private String renterName;
    private String message;
}
