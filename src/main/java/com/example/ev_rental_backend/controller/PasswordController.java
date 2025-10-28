package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.update_profile.UpdatePasswordRequest;
import com.example.ev_rental_backend.service.auth.PasswordUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class PasswordController {

    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordUpdateService passwordUpdateService;

    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdatePasswordRequest request) {

        try {
            // 1️⃣ Lấy token
            String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

            // 2️⃣ Lấy userId & role từ JWT
            Long userId = jwtTokenUtil.extractUserId(token);
            String role = jwtTokenUtil.extractRole(token);

            // 3️⃣ Gọi service
            passwordUpdateService.updatePassword(userId, role, request);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .status("success")
                            .code(200)
                            .data("Cập nhật mật khẩu thành công cho tài khoản " + role)
                            .build()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(500)
                            .data("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }
}
