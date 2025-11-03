package com.example.ev_rental_backend.controller;


import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.update_profile.EmailUpdateConfirmDTO;
import com.example.ev_rental_backend.dto.update_profile.EmailUpdateRequestDTO;
import com.example.ev_rental_backend.service.otp.EmailChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/renter")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
public class EmailChangeController {
    private final EmailChangeService emailChangeService;
    private final JwtTokenUtil jwtTokenUtil;

    // 🟢 1️⃣ Gửi yêu cầu đổi email → hệ thống gửi OTP đến email mới
    @PostMapping("/request-email-change")
    public ResponseEntity<ApiResponse<?>> requestEmailChange(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid EmailUpdateRequestDTO dto) {

        try {
            String token = authHeader.substring(7);
            Long renterId = jwtTokenUtil.extractUserId(token);

            emailChangeService.requestEmailChange(renterId, dto.getEmail());

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .code(200)
                    .data("✅ Đã gửi mã OTP đến email mới: " + dto.getEmail())
                    .build());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        }
    }

    // 🟢 2️⃣ Xác nhận OTP → cập nhật email thật sự
    @PostMapping("/confirm-email-change")
    public ResponseEntity<ApiResponse<?>> confirmEmailChange(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody @Valid EmailUpdateConfirmDTO dto) {

        try {
            String token = authHeader.substring(7);
            Long renterId = jwtTokenUtil.extractUserId(token);

            emailChangeService.confirmEmailChange(renterId, dto.getOtpCode(), dto.getNewEmail());

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .code(200)
                    .data("🎉 Thay đổi email thành công! Vui lòng đăng nhập lại bằng địa chỉ mới.")
                    .build());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .status("error")
                    .code(500)
                    .data("Lỗi hệ thống: " + e.getMessage())
                    .build());
        }
    }
}
