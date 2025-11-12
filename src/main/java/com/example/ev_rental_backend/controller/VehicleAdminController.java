package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.vehicle.VehicleDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;
import com.example.ev_rental_backend.service.vehicle.VehicleImageService;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles")
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
@RequiredArgsConstructor
public class VehicleAdminController {
    private final VehicleService vehicleService;
    private final VehicleImageService vehicleImageService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<VehicleDTO>> create(@Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.<VehicleDTO>builder()
                        .status("success")
                        .code(201)
                        .data(vehicleService.createVehicle(dto))
                        .build()
        );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<VehicleDTO>> update( @PathVariable Long id, @Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(
                ApiResponse.<VehicleDTO>builder()
                        .status("success")
                        .code(200)
                        .data(vehicleService.updateVehicle(id, dto))
                        .build()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<VehicleDTO>>> getAllVehicles() {
        // Giả sử vehicleService.getAllVehicles() tồn tại và trả về List<VehicleDTO>
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles();

        return ResponseEntity.ok(
                ApiResponse.<List<VehicleDTO>>builder()
                        .status("success")
                        .code(200)
                        .data(vehicles)
                        .message("Lấy tất cả phương tiện thành công")
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDetailResponseDTO>> getVehicleById(@PathVariable Long id) {
        VehicleDetailResponseDTO vehicle = vehicleService.getVehicleDetail(id);

        return ResponseEntity.ok(
                ApiResponse.<VehicleDetailResponseDTO>builder()
                        .status("success")
                        .code(200)
                        .data(vehicle)
                        .message("Lấy thông tin xe thành công")
                        .build()
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(200)
                        .data("Vehicle deleted successfully")
                        .build()
        );
    }

    @PostMapping("/{vehicleId}/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadVehicleImage(
            @PathVariable Long vehicleId,
            @RequestParam("file") MultipartFile file) {

        String url = vehicleImageService.uploadVehicleImage(vehicleId, file);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(200)
                        .data(url)
                        .message("Upload ảnh thành công")
                        .build()
        );
    }

}
