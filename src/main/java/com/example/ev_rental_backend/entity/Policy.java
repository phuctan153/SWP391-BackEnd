package com.example.ev_rental_backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
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

    // 🔸 Enum mô tả loại quy định
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_type", nullable = false, length = 50)
    private PolicyType policyType;

    // 🔸 Mô tả ngắn
    @Column(name = "description", length = 255)
    private String description;

    // 🔸 Giá trị cụ thể của quy định (có thể là % hoặc số tiền)
    @Column(name = "value", nullable = false)
    private Double value;

    // 🔸 Phạm vi áp dụng
    @Enumerated(EnumType.STRING)
    @Column(name = "applied_scope", nullable = false, length = 50)
    private AppliedScope appliedScope;

    // 🔸 Trạng thái policy
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;


    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = Status.ACTIVE;
        if (this.appliedScope == null) this.appliedScope = AppliedScope.GLOBAL;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    // 🔹 ENUM: loại quy định
    public enum PolicyType {
        REFUND_PERCENT_RENTER,   // phần trăm hoàn tiền khi renter hủy
        REFUND_PERCENT_ADMIN,    // phần trăm hoàn tiền khi admin hủy
        MIN_DAYS_BEFORE_BOOKING, // số ngày tối thiểu được đặt
        MAX_DAYS_BEFORE_BOOKING, // số ngày tối đa được đặt
        DEPOSIT_AMOUNT,        // tiền cọc
        RENTAL_TIME_THRESHOLD_HOURS,
        VEHICLE_HOLD_DAYS_AFTER_BOOKING,
        MAX_RENTAL_DAYS
    }

    // 🔹 ENUM: phạm vi áp dụng
    public enum AppliedScope {
        GLOBAL, STATION, VEHICLE_TYPE
    }

    // 🔹 ENUM: trạng thái
    public enum Status {
        ACTIVE, INACTIVE
    }
}
