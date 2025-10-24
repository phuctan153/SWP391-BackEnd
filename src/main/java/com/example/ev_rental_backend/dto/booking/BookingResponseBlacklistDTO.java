package com.example.ev_rental_backend.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseBlacklistDTO {
    private Long bookingId;
    private String vehicleName;
    private String vehiclePlateNumber;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String status;
    private Double totalAmount;
    private String depositStatus;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RenterShortInfo {
        private Long renterId;
        private String fullName;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingImageDTO {
        private Long imageId;
        private String imageUrl;
        private String description;
        private String imageType; // BEFORE_RENTAL, AFTER_RENTAL, DAMAGE, OTHER
    }


    private RenterShortInfo renter;
    private List<BookingImageDTO> images;
}
