package com.example.ev_rental_backend.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractRequestDTO {
    private Long bookingId;
    private String contractType;
    private String contractFileUrl; // nếu có
    private List<TermConditionDTO> terms;

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

