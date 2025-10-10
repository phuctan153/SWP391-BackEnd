package com.example.ev_rental_backend.dto.booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingResponseDTO {
    private Long bookingId;
    private Long renterId;
    private Long staffId;
    private Long vehicleId;
    private LocalDate bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime expiredAt;
    private Double depositAmount;
    private Double totalAmount;
}
