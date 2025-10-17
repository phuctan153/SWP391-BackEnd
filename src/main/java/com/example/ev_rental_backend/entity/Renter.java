package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "renter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long renterId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    private LocalDate dateOfBirth;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private boolean isBlacklisted;

    @Column(unique = true)
    private String googleId;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === ENUMS ===
    public enum Status {
        PENDING_VERIFICATION, VERIFIED, DELETED
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    // === AUDIT ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null)
            this.status = Status.PENDING_VERIFICATION;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === RELATIONSHIPS ===

    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToOne(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private RiskProfile riskProfile;

    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OtpVerificationEmail> otps;

    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentityDocument> identityDocuments;
}
