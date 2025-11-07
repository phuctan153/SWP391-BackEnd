package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReportDto {

    // Tổng quan
    private Integer totalVehicles;
    private Integer availableVehicles;
    private Integer inUseVehicles;
    private Integer maintenanceVehicles;
    private Integer inRepairVehicles;

    // Tỷ lệ sử dụng
    private Double utilizationRate; // (inUse / total) * 100

    // Theo trạm
    private List<VehicleByStationDto> vehiclesByStation;

    // Theo model
    private Map<String, Integer> vehiclesByModel;

    // Top vehicles (most booked)
    private List<TopVehicleDto> topVehicles;

    // Vehicles cần bảo trì
    private List<MaintenanceNeededDto> maintenanceNeeded;
}
