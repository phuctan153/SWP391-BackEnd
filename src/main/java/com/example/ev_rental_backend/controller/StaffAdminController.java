package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.staff.StaffDetailDTO;
import com.example.ev_rental_backend.dto.staff.StaffListDTO;
import com.example.ev_rental_backend.service.staff.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/stations")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
public class StaffAdminController {
    private final StaffService staffService;

    @GetMapping("/{stationId}/staff")
    public ResponseEntity<ApiResponse<List<StaffListDTO>>> getStaffByStation(@PathVariable Long stationId) {
        var data = staffService.getStaffByStation(stationId);
        return ResponseEntity.ok(ApiResponse.<List<StaffListDTO>>builder()
                .status("success").code(200).data(data).build());
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffDetailDTO>> getStaffDetail(@PathVariable Long staffId) {
        var detail = staffService.getStaffDetail(staffId);
        return ResponseEntity.ok(ApiResponse.<StaffDetailDTO>builder()
                .status("success").code(200).data(detail).build());
    }
}
