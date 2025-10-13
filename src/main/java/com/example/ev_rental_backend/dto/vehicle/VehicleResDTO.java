package com.example.ev_rental_backend.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResDTO {
    private Long vehicleId;
    private String vehicleName;
    private String plateNumber;
    private Double pricePerHour;
    private Double pricePerDay;
    private Double batteryLevel;
    private Double mileage;
    private String description;
    private String status;

    // Thông tin cơ bản của Station
    private StationBasicDTO station;

    // Thông tin cơ bản của VehicleModel
    private VehicleModelBasicDTO model;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StationBasicDTO {
        private Long stationId;
        private String name;
        private String location;
        private Double latitude;
        private Double longitude;
        private Integer carNumber;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleModelBasicDTO {
        private Long modelId;
        private String modelName;
        private String manufacturer;
        private Double batteryCapacity;
        private Integer seatingCapacity;
    }
}
