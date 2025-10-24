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

    // 🔗 Mối quan hệ 1-1 với Renter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", referencedColumnName = "renterId", unique = true)
    private Renter renter;

    // 💰 Số dư ví
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // 🟢 Trạng thái ví
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.INACTIVE;

    // 🕒 Thời gian tạo ví
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 🕓 Thời gian cập nhật ví
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 📜 Danh sách giao dịch của ví
    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<PaymentTransaction> transactions;

    // 🧩 Khi tạo mới (INSERT)
    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 🔄 Khi cập nhật (UPDATE)
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🔘 Enum trạng thái ví
    public enum Status {
        INACTIVE,  // Renter mới đăng ký (chưa xác minh)
        ACTIVE     // Ví hoạt động
    }
}
