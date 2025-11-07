package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRevenueDto {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private Double revenue;
    private Integer bookingCount;
}
