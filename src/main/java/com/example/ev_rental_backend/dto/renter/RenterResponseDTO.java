package com.example.ev_rental_backend.dto.renter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenterResponseDTO {
    private Long renterId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String driverLicense;
    private LocalDate driverLicenseExpiry;
    private LocalDate dateOfBirth;
    private String address;

    private String status;          // VERIFIED / PENDING_VERIFICATION / DELETED
    private boolean isBlacklisted;

    private String googleId;        // ID Google (nếu đăng nhập bằng Google)
    private String authProvider;    // LOCAL / GOOGLE

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
