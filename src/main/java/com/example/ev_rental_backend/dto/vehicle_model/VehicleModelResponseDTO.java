package com.example.ev_rental_backend.dto.vehicle_model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleModelResponseDTO {
    private Long modelId;
    private String modelName;
    private String manufacturer;
    private Double batteryCapacity;
    private int seatingCapacity;
}

