package com.example.ev_rental_backend.dto.station_vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponseDTO {
    private Long vehicleId;
    private String plateNumber;
    private Double batteryLevel;
    private String status;
    private Double mileage;
    private LocalDate lastServiceDate;
    private String modelName;
}
