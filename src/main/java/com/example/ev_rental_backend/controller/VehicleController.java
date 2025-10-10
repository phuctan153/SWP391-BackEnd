package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
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
}
