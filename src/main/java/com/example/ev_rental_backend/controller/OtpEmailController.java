package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.service.otp.OtpEmailServiceImpl;
import com.example.ev_rental_backend.service.otp.OtpForgotPasswordServiceImpl;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/otp-email")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OtpEmailController {

    private final OtpEmailServiceImpl otpEmailServiceImpl;

    private final RenterRepository renterRepository;

    private final OtpForgotPasswordServiceImpl otpForgotPasswordServiceImpl;

    @PostMapping("/send/{renterId}")
    public ResponseEntity<ApiResponse<?>> sendOtp(@PathVariable Long renterId) {
        try {
            otpEmailServiceImpl.sendOtp(renterId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đã gửi OTP đến email của renter")
                    .build());
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<String>builder()
                    .status("error")
                    .code(500)
                    .data("Lỗi gửi email: " + e.getMessage())
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/verify/{renterId}")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @PathVariable Long renterId,
            @RequestParam String otpCode
    ) {
        try {
            boolean verified = otpEmailServiceImpl.verifyOtp(renterId, otpCode);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Xác thực OTP thành công")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-forgot-password")
    public ResponseEntity<ApiResponse<?>> sendForgotPassword(@RequestParam String email) {
        try {
            otpForgotPasswordServiceImpl.sendForgotPasswordOtp(email);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đã gửi OTP khôi phục mật khẩu đến email: " + email)
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

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword
    ) {
        try {
            Renter renter = renterRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với email này"));

            renter.setPassword(newPassword);
            renterRepository.save(renter);

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đặt lại mật khẩu thành công cho tài khoản: " + email)
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
