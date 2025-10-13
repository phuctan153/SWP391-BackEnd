package com.example.ev_rental_backend.dto.risk_profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskProfileResponseDTO {
    private Long riskId;
    private Long renterId;
    private String riskLevel;
    private int violationCount;
    private LocalDateTime lastViolationAt;
    private String notes;
}