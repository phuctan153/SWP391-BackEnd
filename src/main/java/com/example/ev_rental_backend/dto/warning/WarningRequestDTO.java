package com.example.ev_rental_backend.dto.warning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarningRequestDTO {
    private Long bookingId;
    private String note;
}
