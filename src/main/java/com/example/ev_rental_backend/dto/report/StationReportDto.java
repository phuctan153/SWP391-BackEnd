package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationReportDto {
    // Tổng quan
    private Integer totalStations;
    private Integer activeStations;
    private Integer inactiveStations;

    // Chi tiết từng trạm
    private List<StationDetailDto> stations;

    // Top performing stations
    private List<TopStationDto> topStations;
}
