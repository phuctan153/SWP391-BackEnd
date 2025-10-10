package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "contract_id")
    private String contractId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "contract_date", nullable = false)
    private LocalDateTime contractDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;

    @Column(name = "renter_signature", columnDefinition = "TEXT")
    private String renterSignature;

    @Column(name = "staff_signature", columnDefinition = "TEXT")
    private String staffSignature;

    @Column(name = "pickup_condition", columnDefinition = "TEXT")
    private String pickupCondition;

    @Column(name = "return_condition", columnDefinition = "TEXT")
    private String returnCondition;

    @Column(name = "pickup_mileage")
    private Integer pickupMileage;

    @Column(name = "return_mileage")
    private Integer returnMileage;

    @Column(name = "pickup_battery_level")
    private Integer pickupBatteryLevel;

    @Column(name = "return_battery_level")
    private Integer returnBatteryLevel;

    @Column(name = "pickup_images", columnDefinition = "TEXT")
    private String pickupImages;

    @Column(name = "return_images", columnDefinition = "TEXT")
    private String returnImages;

    @Column(name = "damage_notes", columnDefinition = "TEXT")
    private String damageNotes;

    @OneToOne(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OtpVerification otpVerification;
}

enum ContractType {
    ELECTRONIC, PAPER
}

