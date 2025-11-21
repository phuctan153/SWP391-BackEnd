package com.example.ev_rental_backend.dto.station_vehicle;

import lombok.Data;

@Data
public class DispatchRequestDTO {
    private Long staffId;
    private Long sourceStationId;
    private Long targetStationId;
}