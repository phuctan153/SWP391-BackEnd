package com.example.ev_rental_backend.dto.booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDTO {
    private Long bookingId;
    private String status;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String note;
    private RenterBasicDTO renter;
    private VehicleBasicDTO vehicle;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RenterBasicDTO {
        private Long renterId;
        private String fullName;
        private String email;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VehicleBasicDTO {
        private Long vehicleId;
        private String plateNumber;
        private String modelName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StationBasicDTO {
        private Long stationId;
        private String name;
        private String location;
    }
}
