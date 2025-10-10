package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @OneToOne @JoinColumn(name = "contract_id")
    private Contract contract;

    private String otpCode;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime verifiedAt;
    private int attemptCount;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { PENDING, VERIFIED, FAILED }
}

