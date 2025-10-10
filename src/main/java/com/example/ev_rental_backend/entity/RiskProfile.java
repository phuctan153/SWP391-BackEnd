package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riskId;

    @OneToOne @JoinColumn(name = "renter_id")
    private Renter renter;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private int violationCount;
    private LocalDateTime lastViolationAt;
    private String notes;

    public enum RiskLevel { LOW, MEDIUM, HIGH }
}

