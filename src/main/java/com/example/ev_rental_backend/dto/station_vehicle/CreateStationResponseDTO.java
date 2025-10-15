package com.example.ev_rental_backend.dto.station_vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateStationResponseDTO {
    private Long stationId;
    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer carNumber;
    private String status;

    // Thông tin thống kê (optional)
    private Integer currentVehicleCount;
    private Integer availableVehicleCount;

    // Danh sách xe (optional, có thể null khi tạo mới)
    private List<VehicleBasicDTO> vehicles;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VehicleBasicDTO {
        private Long vehicleId;
        private String vehicleName;
        private String plateNumber;
        private Double batteryLevel;
        private String status;
    }

}
