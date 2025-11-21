package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;
import com.example.ev_rental_backend.service.vehicle.VehicleModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    /**
     * 📋 Lấy tất cả model xe
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleModelResponseDTO>>> getAllVehicleModels() {
        List<VehicleModelResponseDTO> models = vehicleModelService.getAllVehicleModels();
        return ResponseEntity.ok(
                ApiResponse.<List<VehicleModelResponseDTO>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(models)
                        .build()
        );
    }

    /**
     * 🔍 Lấy model theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleModelResponseDTO>> getVehicleModelById(@PathVariable Long id) {
        VehicleModelResponseDTO model = vehicleModelService.getVehicleModelById(id);
        return ResponseEntity.ok(
                ApiResponse.<VehicleModelResponseDTO>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(model)
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VehicleModelResponseDTO>> createVehicleModel(
            @Valid @RequestBody VehicleModelRequestDTO requestDTO
    ) {
        VehicleModelResponseDTO model = vehicleModelService.createVehicleModel(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<VehicleModelResponseDTO>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(model)
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleModelResponseDTO>> updateVehicleModel(
            @PathVariable Long id,
            @Valid @RequestBody VehicleModelRequestDTO requestDTO
    ) {
        VehicleModelResponseDTO updated = vehicleModelService.updateVehicleModel(id, requestDTO);
        return ResponseEntity.ok(
                ApiResponse.<VehicleModelResponseDTO>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(updated)
                        .build()
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVehicleModel(@PathVariable Long id) {
        vehicleModelService.deleteVehicleModel(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data("Đã xóa model xe có ID = " + id)
                        .build()
        );
    }
}
