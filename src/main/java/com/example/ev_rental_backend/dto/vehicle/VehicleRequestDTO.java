package com.example.ev_rental_backend.dto.vehicle;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequestDTO {
    @NotBlank(message = "Vihicle name Not be blank")
    @Size(max = 100, message = "Vehicle name must be less than 100 characters")
    private String vehicleName;

    @NotBlank(message = "Plate number Not be blank")
    @Size(max = 20, message = "Plate number must be less than 20 characters")
    private String plateNumber;

    @NotNull(message = "Price per hour Not be null")
    @DecimalMin(value = "0.0", message = "Price per hour must be >= 0")
    private Double pricePerHour;

    @NotNull(message = "Price per day Not be null")
    @DecimalMin(value = "0.0", message = "Giá thuê theo ngày phải >= 0")
    private Double pricePerDay;

    @NotNull(message = "Mức pin không được để trống")
    @DecimalMin(value = "0.0", message = "Mức pin phải >= 0")
    @DecimalMax(value = "100.0", message = "Mức pin phải <= 100")
    private Double batteryLevel;

    @NotNull(message = "Quãng đường không được để trống")
    @DecimalMin(value = "0.0", message = "Quãng đường phải >= 0")
    private Double mileage;

    @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
    private String description;

    private String status; // Optional, mặc định là AVAILABLE

    @NotNull(message = "ID trạm không được để trống")
    @Positive(message = "ID trạm phải là số dương")
    private Long stationId;

    @NotNull(message = "ID model xe không được để trống")
    @Positive(message = "ID model xe phải là số dương")
    private Long modelId;
}
