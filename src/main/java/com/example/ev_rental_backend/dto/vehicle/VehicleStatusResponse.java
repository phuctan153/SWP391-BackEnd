package com.example.ev_rental_backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleStatusResponse {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
