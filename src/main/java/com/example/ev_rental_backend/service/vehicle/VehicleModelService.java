package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;
import java.util.List;

public interface VehicleModelService {

    VehicleModelResponseDTO createVehicleModel(VehicleModelRequestDTO requestDTO);

    List<VehicleModelResponseDTO> getAllVehicleModels();

    VehicleModelResponseDTO getVehicleModelById(Long id);

    VehicleModelResponseDTO updateVehicleModel(Long id, VehicleModelRequestDTO requestDTO);

    void deleteVehicleModel(Long id);
}
