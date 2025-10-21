package com.example.ev_rental_backend.dto.contract;

import lombok.Data;

@Data
public class AdminContractSignDTO {
    private Long contractId;
    private Long adminId;
    private String otpCode;
    private boolean approved;
}