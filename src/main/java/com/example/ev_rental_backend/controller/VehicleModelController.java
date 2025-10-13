package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;
import com.example.ev_rental_backend.service.vehicle.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
public class VehicleModelController {
    private final VehicleModelService vehicleModelService;

    @PostMapping("/create")
    public ResponseEntity<VehicleModelResponseDTO> createVehicleModel(@RequestBody VehicleModelRequestDTO requestDTO) {
        return ResponseEntity.ok(vehicleModelService.createVehicleModel(requestDTO));
    }
}
