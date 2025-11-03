package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.station_vehicle.CreateStationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.service.station.StationService;
import jakarta.validation.Valid;
import com.example.ev_rental_backend.service.vehicle.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final VehicleService vehicleService;

    /**
     * Lấy danh sách trạm có xe khả dụng, sắp xếp theo khoảng cách và số xe.
     * Ví dụ: GET /api/stations?lat=10.7769&lng=106.7009
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StationResponseDTO>>> getStations(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        List<StationResponseDTO> stations = stationService.getStationsByLocation(lat, lng);

        ApiResponse<List<StationResponseDTO>> response = ApiResponse.<List<StationResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(stations)
                .build();

        return ResponseEntity.ok(response);
    }

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

    /**
     * Trường hợp không có tham số (fallback)
     * -> Mặc định trả về tất cả trạm có xe khả dụng (không sắp xếp theo khoảng cách)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StationResponseDTO>>> getAllStations() {
        List<StationResponseDTO> stations = stationService.getAllStations();

        ApiResponse<List<StationResponseDTO>> response = ApiResponse.<List<StationResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(stations)
                .build();

        return ResponseEntity.ok(response);
    }

    //API tạo mới trạm
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateStationResponseDTO>> createStation(
            @Valid @RequestBody StationRequestDTO requestDTO
    ) {
        CreateStationResponseDTO stationData = stationService.createStation(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<CreateStationResponseDTO>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(stationData)
                        .build()
        );
    }

    /**
     * ✏️ Cập nhật trạm — chỉ Admin được phép
     * Ví dụ: PUT /api/stations/5
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{stationId}")
    public ResponseEntity<ApiResponse<StationResponseDTO>> updateStation(
            @PathVariable Long stationId,
            @Valid @RequestBody StationRequestDTO requestDTO
    ) {
        StationResponseDTO updatedStation = stationService.updateStation(stationId, requestDTO);

        return ResponseEntity.ok(
                ApiResponse.<StationResponseDTO>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(updatedStation)
                        .build()
        );
    }

    /**
     * 🗑️ Xóa (vô hiệu hóa) trạm — chỉ Admin được phép
     * Ví dụ: DELETE /api/stations/5
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{stationId}")
    public ResponseEntity<ApiResponse<String>> deleteStation(@PathVariable Long stationId) {
        stationService.deleteStation(stationId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data("Đã vô hiệu hóa trạm có ID = " + stationId)
                        .build()
        );
    }

}
