package com.example.ev_rental_backend.controller;


import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.service.renter.RenterService;
import com.example.ev_rental_backend.service.renter.RenterServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/renters")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class StaffController {

    private final RenterService renterService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<RenterResponseDTO>>> getPendingRenters() {
        List<RenterResponseDTO> pendingRenters = renterService.getPendingVerificationRenters();

        ApiResponse<List<RenterResponseDTO>> response = ApiResponse.<List<RenterResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(pendingRenters)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{renterId}/verify")
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

    @DeleteMapping("/{renterId}/delete")
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
