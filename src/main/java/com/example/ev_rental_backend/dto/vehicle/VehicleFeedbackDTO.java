package com.example.ev_rental_backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFeedbackDTO {
    private Long ratingId;
    private String renterName;
    private int vehicleRating;
    private String comment;
    private LocalDateTime createdAt;
}
