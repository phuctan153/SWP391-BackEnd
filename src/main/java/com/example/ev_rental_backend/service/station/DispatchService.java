package com.example.ev_rental_backend.service.station;

import com.example.ev_rental_backend.dto.station_vehicle.DispatchRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationLoadDTO;

import java.util.List;

public interface DispatchService {
    public List<StationLoadDTO> getOverloadedStations();

    public String assignStaff(DispatchRequestDTO dto);
}
