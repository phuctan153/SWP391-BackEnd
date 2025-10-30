package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
