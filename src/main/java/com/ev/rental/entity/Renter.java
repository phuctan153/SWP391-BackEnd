package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "renters")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Renter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "renter_id")
    private String renterId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "national_id", nullable = false)
    private String nationalId;

    @Column(name = "national_id_images", columnDefinition = "TEXT")
    private String nationalIdImages;

    @Column(name = "driver_license", nullable = false)
    private String driverLicense;

    @Column(name = "driver_license_images", columnDefinition = "TEXT")
    private String driverLicenseImages;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RenterStatus status = RenterStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne(mappedBy = "renter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RiskProfile riskProfile;

    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Booking> bookings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

enum RenterStatus {
    ACTIVE, PENDING, BLOCKED
}
