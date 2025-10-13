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

    // 🔗 N–1: nhiều OTP có thể thuộc về 1 hợp đồng
    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // 🔢 Mã OTP
    @Column(length = 6, nullable = false)
    private String otpCode;

    // ⏰ Thời gian tạo và hết hạn
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    // 🕒 Thời điểm xác thực thành công
    private LocalDateTime verifiedAt;

    // 🔁 Số lần nhập sai OTP
    private int attemptCount;

    // ⚙️ Trạng thái OTP
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    public enum Status {
        PENDING, VERIFIED, FAILED
    }

    // 🕒 Tự động set thời gian tạo & hết hạn
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiredAt == null) {
            this.expiredAt = this.createdAt.plusMinutes(5); // OTP hết hạn sau 5 phút
        }
        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }
}
