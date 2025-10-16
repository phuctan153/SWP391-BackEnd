package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.forgot_password.ForgotPasswordRequestDTO;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.service.otp.OtpEmailServiceImpl;
import com.example.ev_rental_backend.service.otp.OtpForgotPasswordServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
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

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody @Valid ForgotPasswordRequestDTO dto) {
        String email = dto.getEmail();
        try {
            otpEmailServiceImpl.sendOtpByEmail(email);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Đã gửi OTP đến email: " + email)
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


    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otpCode
    ) {
        try {
            boolean verified = otpEmailServiceImpl.verifyOtpByEmail(email, otpCode);
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


}
