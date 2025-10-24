package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.forgot_password.ForgotPasswordRequestDTO;
import com.example.ev_rental_backend.dto.forgot_password.ResetPasswordWithOtpDTO;
import com.example.ev_rental_backend.service.otp.OtpForgotPasswordService;
import com.example.ev_rental_backend.service.otp.OtpForgotPasswordServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/forgot-password")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OtpForgotPasswordController {

    private final OtpForgotPasswordService otpForgotPasswordService;

    // ✅ Bước 1: Gửi OTP đến email người dùng
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendForgotPasswordOtp(@RequestBody @Valid ForgotPasswordRequestDTO dto) {
        try {
            otpForgotPasswordService.sendForgotPasswordOtp(dto.getEmail());
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đã gửi OTP khôi phục mật khẩu đến email: " + dto.getEmail())
                    .build());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .status("error")
                            .code(500)
                            .data("Lỗi gửi email: " + e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build());
        }
    }

    // ✅ Bước 2: Xác thực OTP và đổi mật khẩu
    @PostMapping("/verify-reset")
    public ResponseEntity<ApiResponse<?>> verifyOtpAndResetPassword(@RequestBody @Valid ResetPasswordWithOtpDTO dto) {
        try {
            otpForgotPasswordService.verifyOtpAndResetPassword(dto);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đặt lại mật khẩu thành công cho tài khoản: " + dto.getEmail())
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build());
        }
    }
}
