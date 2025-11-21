package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.station_vehicle.DispatchRequestDTO;
import com.example.ev_rental_backend.service.station.DispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dispatch")
@RequiredArgsConstructor
public class DispatchController {
    private final DispatchService dispatchService;

    // ✅ API: xem các trạm đang quá tải
    @GetMapping("/overloaded")
    public ResponseEntity<ApiResponse<?>> getOverloadedStations() {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .code(200)
                        .data(dispatchService.getOverloadedStations())
                        .build()
        );
    }

    // ✅ API: điều phối nhân viên từ trạm rảnh sang trạm quá tải
    @PostMapping("/assign-staff")
    public ResponseEntity<ApiResponse<?>> assignStaff(@RequestBody DispatchRequestDTO dto) {
        try {
            String result = dispatchService.assignStaff(dto);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .code(200)
                            .data(result)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status("error")
                            .code(400)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
