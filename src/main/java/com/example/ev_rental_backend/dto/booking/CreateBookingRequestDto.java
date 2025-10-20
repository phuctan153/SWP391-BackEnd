package com.example.ev_rental_backend.dto.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequestDto {
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Start date time is required")
    @Future(message = "Start date time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date time is required")
    @Future(message = "End date time must be in the future")
    private LocalDateTime endDateTime;
}
