package com.example.ev_rental_backend.service.station;

import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;

import java.util.List;

public interface StationService {
    List<StationResponseDTO> getStationsByLocation(double userLat, double userLng);
    public List<StationResponseDTO> getAllStations();
}
