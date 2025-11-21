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
public class BookingWithContractDTO {
    private Long bookingId;
    private String vehicleName;
    private String stationName;
    private String renterName;
    private String bookingStatus;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // Thông tin hợp đồng
    private Long contractId;
    private String contractStatus;
    private String contractFileUrl;
    private LocalDateTime renterSignedAt;
    private LocalDateTime staffSignedAt;
}
