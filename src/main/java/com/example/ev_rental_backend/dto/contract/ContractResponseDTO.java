package com.example.ev_rental_backend.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponseDTO {
    private Long contractId;
    private Long bookingId;
    private LocalDate contractDate;
    private String contractType;
    private String termsConditions;
    private String renterSignature;
    private String staffSignature;
}

