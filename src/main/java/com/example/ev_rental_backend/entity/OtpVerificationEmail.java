package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification_email")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    @Column(nullable = false, length = 6)
    private String otpCode;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        UNVERIFIED, VERIFIED, EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusMinutes(5);
        this.status = Status.UNVERIFIED;
    }
}
