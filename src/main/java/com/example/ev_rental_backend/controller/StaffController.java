package com.example.ev_rental_backend.controller;


import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.service.renter.RenterService;
import com.example.ev_rental_backend.service.renter.RenterServiceImpl;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class StaffController {

    private final RenterService renterService;
    private final VehicleService vehicleService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/my-station")
    public ResponseEntity<ApiResponse<?>> getMyStation(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            Long staffId = jwtTokenUtil.extractUserId(token);

            List<VehicleResponseDTO> vehicleList = vehicleService.getVehiclesByCurrentStaffStation(staffId);

            ApiResponse<List<VehicleResponseDTO>> response = ApiResponse.<List<VehicleResponseDTO>>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data(vehicleList)
                    .message("Fetched vehicle list successfully.")
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            ApiResponse<String> error = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .data(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            ApiResponse<String> error = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data("Lỗi hệ thống: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/renters")
    public ResponseEntity<ApiResponse<?>> getRentersByStatus(
            @RequestParam(name = "status", required = false) String status) {
        try {
            List<RenterResponseDTO> renters;

            if (status == null || status.isBlank()) {
                renters = renterService.getAllRenters(); // lấy toàn bộ nếu không truyền status
            } else {
                renters = renterService.getRentersByStatus(status.toUpperCase());
            }

            ApiResponse<List<RenterResponseDTO>> response = ApiResponse.<List<RenterResponseDTO>>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data(renters)
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

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<ApiResponse<?>> getRenterDetailById(@PathVariable Long renterId) {
        try {
            RenterResponseDTO renterDetail = renterService.getRenterDetailById(renterId);

            ApiResponse<RenterResponseDTO> response = ApiResponse.<RenterResponseDTO>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data(renterDetail)
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



    @PutMapping("/renter/{renterId}/verify")
    public ResponseEntity<ApiResponse<?>> verifyRenter(@PathVariable Long renterId) {
        try {
            RenterResponseDTO verifiedRenter = renterService.verifyRenterById(renterId);

            ApiResponse<RenterResponseDTO> response = ApiResponse.<RenterResponseDTO>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data(verifiedRenter)
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

    @DeleteMapping("/renter/{renterId}/delete")
    public ResponseEntity<ApiResponse<?>> deleteRenter(@PathVariable Long renterId) {
        try {
            renterService.deleteRenterById(renterId);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .data("Đã xóa người thuê có ID: " + renterId)
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
