package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;

import java.util.List;

public interface VehicleService {
    List<VehicleResponseDTO> getVehiclesByStationId(Long stationId);

    VehicleDetailResponseDTO getVehicleDetail(Long vehicleId);
}
