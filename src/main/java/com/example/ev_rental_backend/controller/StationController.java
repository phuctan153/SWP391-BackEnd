package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.station_vehicle.CreateStationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.service.station.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

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
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateStationResponseDTO>> createStation(
            @Valid @RequestBody StationRequestDTO requestDTO) {

        CreateStationResponseDTO stationData = stationService.createStation(requestDTO);

        ApiResponse<CreateStationResponseDTO> response = ApiResponse.<CreateStationResponseDTO>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(stationData)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
