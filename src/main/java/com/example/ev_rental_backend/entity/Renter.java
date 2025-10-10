package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
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

    // Cho phép null nếu đăng nhập bằng Google
    @Column(length = 255, nullable = true)
    private String password;


    @Column(nullable = true, unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 20)
    private String nationalId;

    @Column(length = 20)
    private String driverLicense;

    private LocalDate driverLicenseExpiry; // ✅ Thêm mới

    private LocalDate dateOfBirth;
    private String address;

    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean isBlacklisted;

    @Column(unique = true)
    private String googleId;  // Lưu sub từ Google

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider; // LOCAL hoặc GOOGLE

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        VERIFIED, PENDING_VERIFICATION, DELETED
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    @OneToOne(mappedBy = "renter", cascade = CascadeType.ALL, orphanRemoval = true)
    private RiskProfile riskProfile;
}
