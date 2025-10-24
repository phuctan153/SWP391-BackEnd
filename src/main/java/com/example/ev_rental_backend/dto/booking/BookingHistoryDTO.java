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
public class BookingHistoryDTO {
    private Long bookingId;
    private String renterName;
    private String renterEmail;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double totalAmount;
    private String bookingStatus;
}
