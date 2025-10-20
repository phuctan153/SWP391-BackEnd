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
public class BookingRatingResponseDto {

    private Long ratingId;
    private Long bookingId;
    private Integer vehicleRating;
    private Integer staffRating;
    private String comment;
    private LocalDateTime createdAt;
}
