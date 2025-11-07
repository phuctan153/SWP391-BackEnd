package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopVehicleDto {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private Integer bookingCount;
    private Double averageRating;
    private Double totalRevenue;
}
