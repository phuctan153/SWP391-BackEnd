package com.example.ev_rental_backend.dto.renter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenterResponseDTO {

    private Long renterId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;

    // ✅ Trạng thái hệ thống
    private String status;          // VERIFIED / PENDING_VERIFICATION / DELETED
    private boolean isBlacklisted;

    // ✅ Thông tin đăng nhập
    private String googleId;        // ID Google (nếu có)
    private String authProvider;    // LOCAL / GOOGLE

    // ✅ Trạng thái xác thực
    private String otpStatus;       // VERIFIED / PENDING
    private String kycStatus;       // VERIFIED / WAITING_APPROVAL / NEED_UPLOAD / REJECTED
    private String nextStep;        // EMAIL_OTP / KYC / DASHBOARD

    // ✅ Giấy tờ tuỳ thân
    private IdentityDocDTO cccd;
    private IdentityDocDTO gplx;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IdentityDocDTO {
        private String documentNumber;
        private String fullName;
        private String type;          // CCCD / GPLX
        private String status;        // VERIFIED / PENDING / REJECTED
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private LocalDateTime verifiedAt;
    }
}
