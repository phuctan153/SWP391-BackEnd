package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleStatusResponse;
import com.example.ev_rental_backend.dto.vehicle.VehicleStatusUpdate;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * 📦 Lấy tất cả xe thuộc 1 trạm
     * Ví dụ: GET /api/stations/1/vehicles
     */
    @GetMapping("/{stationId}/vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponseDTO>>> getVehiclesByStationId(
            @PathVariable Long stationId
    ) {
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByStationId(stationId);

        ApiResponse<List<VehicleResponseDTO>> response = ApiResponse.<List<VehicleResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(vehicles)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VehicleResDTO>> createVehicle(
            @Valid @RequestBody VehicleRequestDTO requestDTO) {

        VehicleResDTO vehicleData = vehicleService.createVehicle(requestDTO);

        ApiResponse<VehicleResDTO> response = ApiResponse.<VehicleResDTO>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(vehicleData)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VehicleStatusResponse>> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody VehicleStatusUpdate requestDTO) {

        VehicleStatusResponse statusData = vehicleService.updateVehicleStatus(id, requestDTO);

        ApiResponse<VehicleStatusResponse> response = ApiResponse.<VehicleStatusResponse>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(statusData)
                .build();

        return ResponseEntity.ok(response);
    }
}
