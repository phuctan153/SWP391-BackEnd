package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;

public interface VehicleModelService {
    VehicleModelResponseDTO createVehicleModel(VehicleModelRequestDTO requestDTO);
}
