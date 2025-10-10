package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "risk_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "risk_id")
    private String riskId;

    @OneToOne
    @JoinColumn(name = "renter_id", nullable = false, unique = true)
    private Renter renter;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "violation_count")
    private Integer violationCount = 0;

    @Column(name = "last_violation_date")
    private LocalDate lastViolationDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

enum RiskLevel {
    LOW, MEDIUM, HIGH
}
