package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle")
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
@RequiredArgsConstructor
public class VehicleDetailController {

    private final VehicleService vehicleDetailService;

    @GetMapping("/detail/{vehicleId}")
    public ResponseEntity<ApiResponse<?>> getVehicleDetail(@PathVariable Long vehicleId) {
        try {
            VehicleDetailResponseDTO vehicleDetail = vehicleDetailService.getVehicleDetail(vehicleId);

            ApiResponse<VehicleDetailResponseDTO> response = ApiResponse.<VehicleDetailResponseDTO>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data(vehicleDetail)
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .data(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data("Lỗi hệ thống: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
