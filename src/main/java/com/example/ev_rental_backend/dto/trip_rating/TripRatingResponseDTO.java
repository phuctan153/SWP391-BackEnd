package com.example.ev_rental_backend.dto.trip_rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRatingResponseDTO {
    private Long ratingId;
    private Long bookingId;
    private int renterRating;
    private int staffRating;
    private String comment;
    private LocalDateTime createdAt;
}

