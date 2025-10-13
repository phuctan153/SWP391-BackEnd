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
public class KycVerificationDTO {

    private Long renterId;

    // ---------- CCCD ----------
    private String nationalId;
    private String nationalName;
    private LocalDate nationalDob;
    private String nationalAddress;
    private LocalDate nationalExpireDate;

    // ---------- GPLX ----------
    private String driverLicense;
    private String driverName;
    private String driverAddress;
    private String driverClass;
    private LocalDate driverIssueDate;
    private LocalDate driverExpireDate;

    // ---------- Optional ----------
    private double confidenceScore; // overall_score nếu OCR có trả
}
