package com.example.ev_rental_backend.dto.otp_verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationResponseDTO {
    private Long otpId;
    private Long contractId;
    private String otpCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime verifiedAt;
    private int attemptCount;
    private String status;
}

