package com.example.ev_rental_backend.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDTO {
    private Long renterId;
    private Long vehicleId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}