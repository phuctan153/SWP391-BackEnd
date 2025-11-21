package com.example.ev_rental_backend.dto.station_vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationLoadDTO {
    private Long stationId;
    private String stationName;
    private int activeBookings;
    private int staffCount;
}