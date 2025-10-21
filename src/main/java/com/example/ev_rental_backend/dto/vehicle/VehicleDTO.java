package com.example.ev_rental_backend.dto.vehicle;

import com.example.ev_rental_backend.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {
    private Long vehicleId;
    private String vehicleName;
    private Long stationId;
    private String stationName;
    private Long modelId;
    private String modelName;
    private Double pricePerHour;
    private Double pricePerDay;
    private String plateNumber;
    private Double batteryLevel;
    private Double mileage;
    private String description;
    private Vehicle.Status status;
}
