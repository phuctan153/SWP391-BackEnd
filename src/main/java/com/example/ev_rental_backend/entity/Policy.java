package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Column(name = "description", length = 255)
    private String description;

    // 🔸 Phần trăm hoàn tiền khi renter hủy
    @Column(name = "refund_percent_renter", nullable = false)
    private Double refundPercentRenter;

    // 🔸 Phần trăm hoàn tiền khi admin hủy
    @Column(name = "refund_percent_admin", nullable = false)
    private Double refundPercentAdmin;

    // 🔸 Số ngày tối thiểu được đặt trước
    @Column(name = "min_days_before_booking", nullable = false)
    private Integer minDaysBeforeBooking;

    // 🔸 Số ngày tối đa được đặt trước
    @Column(name = "max_days_before_booking", nullable = false)
    private Integer maxDaysBeforeBooking;

    // 🔸 Tiền cọc
    @Column(name = "deposit_amount", nullable = false)
    private Double depositAmount;

    // ✅ Cho phép nhiều policy hoạt động song song
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // ✅ Có thể gắn policy này cho loại xe hoặc trạm trong tương lai
    @Column(name = "applied_scope", length = 50)
    private String appliedScope; // VD: "GLOBAL", "STATION", "VEHICLE_TYPE"

    // ✅ Ngày tạo & cập nhật
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = (this.status == null) ? Status.ACTIVE : this.status;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
