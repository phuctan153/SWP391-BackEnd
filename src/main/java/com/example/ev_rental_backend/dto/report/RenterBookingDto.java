package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenterBookingDto {
    private Long renterId;
    private String renterName;
    private String email;
    private Integer bookingCount;
    private Double totalSpent;
}
