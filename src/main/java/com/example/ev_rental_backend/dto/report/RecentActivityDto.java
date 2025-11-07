package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {
    private String type; // BOOKING_CREATED, PAYMENT_SUCCESS, etc.
    private String description;
    private LocalDateTime timestamp;
}
