package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;

import java.util.List;

public interface VehicleService {
    List<VehicleResponseDTO> getVehiclesByStationId(Long stationId);

    VehicleResDTO createVehicle(VehicleRequestDTO requestDTO);
}
