package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopStationDto {
    private Long stationId;
    private String name;
    private Integer bookingCount;
    private Double revenue;
    private Double utilizationRate;
}
