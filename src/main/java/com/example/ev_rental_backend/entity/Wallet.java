package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    // Mỗi Renter chỉ có 1 ví duy nhất (1-1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", referencedColumnName = "renterId", unique = true)
    private Renter renter;

    // Số dư ví
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // Trạng thái ví (INACTIVE khi renter mới đăng ký, ACTIVE sau khi staff xác minh)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.INACTIVE;

    // Thời gian tạo ví
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Thời gian cập nhật ví
    @Column(nullable = true)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Danh sách các giao dịch liên quan đến ví
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PaymentTransaction> transactions;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status {
        INACTIVE,  // Renter mới đăng ký (chưa xác minh giấy tờ)
        ACTIVE     // Ví hoạt động sau khi staff xác minh KYC
    }
}
