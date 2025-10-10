package com.example.ev_rental_backend.dto.renter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private LocalDate dateOfBirth;
    private String address;
    private String status;
    private boolean isBlacklisted;
}
