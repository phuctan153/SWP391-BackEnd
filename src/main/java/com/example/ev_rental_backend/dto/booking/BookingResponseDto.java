package com.example.ev_rental_backend.dto.booking;
import com.example.ev_rental_backend.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {

    private Long bookingId;
    private Long renterId;
    private String renterName;
    private Long vehicleId;
    private String vehicleName;
    private Long staffId;
    private String staffName;

    private Double priceSnapshotPerHour;
    private Double priceSnapshotPerDay;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalDateTime actualReturnTime;

    private Double totalAmount;

    private Booking.Status status;
    private Booking.DepositStatus depositStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
