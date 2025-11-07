package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.admin.AdminResponseDto;
import com.example.ev_rental_backend.dto.admin.CreateAdminDto;
import com.example.ev_rental_backend.dto.admin.UpdateAdminDto;
import com.example.ev_rental_backend.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 11.1. Admin Profile ====================

    /**
     * GET /api/admin/me - Thông tin admin hiện tại
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponseDto>> getMyProfile() {
        AdminResponseDto admin = adminService.getCurrentAdmin();
        return ResponseEntity.ok(ApiResponse.<AdminResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(admin)
                .build());
    }

    /**
     * PUT /api/admin/me - Cập nhật thông tin admin
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponseDto>> updateMyProfile(
            @Valid @RequestBody UpdateAdminDto requestDto) {
        AdminResponseDto admin = adminService.updateCurrentAdmin(requestDto);
        return ResponseEntity.ok(ApiResponse.<AdminResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(admin)
                .build());
    }

    // ==================== 11.2. Admin Operations ====================

    /**
     * GET /api/admin - Danh sách admin
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminResponseDto>>> getAllAdmins() {
        List<AdminResponseDto> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.<List<AdminResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(admins)
                .build());
    }

    /**
     * POST /api/admin - Tạo admin mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponseDto>> createAdmin(
            @Valid @RequestBody CreateAdminDto requestDto) {
        AdminResponseDto admin = adminService.createAdmin(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AdminResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(admin)
                        .build());
    }

    /**
     * PUT /api/admin/{adminId} - Cập nhật admin
     */
    @PutMapping("/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminResponseDto>> updateAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminDto requestDto) {
        AdminResponseDto admin = adminService.updateAdmin(adminId, requestDto);
        return ResponseEntity.ok(ApiResponse.<AdminResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(admin)
                .build());
    }
}
