package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueByDateDto {
    private String date; // yyyy-MM-dd or yyyy-MM or yyyy
    private Double revenue;
    private Integer bookingCount;
}
