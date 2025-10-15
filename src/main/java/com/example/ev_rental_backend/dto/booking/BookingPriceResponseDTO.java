package com.example.ev_rental_backend.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPriceResponseDTO {
    private Double totalAmount;
    private Double totalHours;
    private Double pricePerHour;
    private Double pricePerDay;
    private String message;
}
