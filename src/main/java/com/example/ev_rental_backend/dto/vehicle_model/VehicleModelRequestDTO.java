package com.example.ev_rental_backend.dto.vehicle_model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleModelRequestDTO {
    private String modelName;
    private String manufacturer;
    private Double batteryCapacity;
    private int seatingCapacity;
}
