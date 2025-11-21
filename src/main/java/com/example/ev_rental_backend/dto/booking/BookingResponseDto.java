package com.example.ev_rental_backend.dto.booking;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.BookingImage;
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
public class BookingResponseDto {

    private Long bookingId;
    private Long renterId;
    private String renterName;
    private Long vehicleId;
    private String vehicleName;

    private Long staffReceiveId;
    private String staffReceiveName;

    private Long staffReturnId;
    private String staffReturnName;

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

    private List<BookingImageDto> bookingImages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingImageDto {
        private Long imageId;
        private String imageUrl;
        private String description;
        private LocalDateTime createdAt;
        private BookingImage.ImageType imageType;
        private BookingImage.VehicleComponent vehicleComponent;
    }
}
