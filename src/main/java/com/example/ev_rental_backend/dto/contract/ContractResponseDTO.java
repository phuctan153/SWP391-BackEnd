package com.example.ev_rental_backend.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractResponseDTO {

    private Long contractId;
    private Long bookingId;
    private String contractType;
    private String contractFileUrl;
    private String status;
    private LocalDateTime contractDate;
    private List<TermConditionDTO> terms;

    private String adminName;
    private String renterName;

    private LocalDateTime adminSignedAt;
    private LocalDateTime renterSignedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TermConditionDTO {
        private Integer termNumber;
        private String termTitle;
        private String termContent;
    }
}