package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleByStationDto {
    private Long stationId;
    private String stationName;
    private Integer totalVehicles;
    private Integer availableVehicles;
    private Integer inUseVehicles;
}
