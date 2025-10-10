package com.example.ev_rental_backend.dto.station_vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationResponseDTO {
    private Long stationId;
    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private int capacity;
    private String status;
    private List<VehicleResponseDTO> vehicles;

    private Double distance;
    private Integer availableCount;
}