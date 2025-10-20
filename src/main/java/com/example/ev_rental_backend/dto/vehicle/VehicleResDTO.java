package com.example.ev_rental_backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResDTO {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private Double pricePerHour;
    private Double pricePerDay;
    private String stationName;
    private String status;
}
