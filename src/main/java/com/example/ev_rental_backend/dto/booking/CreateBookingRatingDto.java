package com.example.ev_rental_backend.dto.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRatingDto {

    @NotNull(message = "Vehicle rating is required")
    @Min(value = 1, message = "Vehicle rating must be at least 1")
    @Max(value = 5, message = "Vehicle rating must be at most 5")
    private Integer vehicleRating;

    @NotNull(message = "Staff rating is required")
    @Min(value = 1, message = "Staff rating must be at least 1")
    @Max(value = 5, message = "Staff rating must be at most 5")
    private Integer staffRating;

    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String comment;
}
