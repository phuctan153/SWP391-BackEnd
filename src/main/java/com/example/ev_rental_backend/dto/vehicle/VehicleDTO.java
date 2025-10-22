package com.example.ev_rental_backend.dto.vehicle;

import com.example.ev_rental_backend.entity.Vehicle;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {

    private Long vehicleId;

    @NotBlank(message = "Tên xe không được để trống")
    @Size(max = 100, message = "Tên xe không được vượt quá 100 ký tự")
    private String vehicleName;

    @NotNull(message = "Trạm xe không được để trống")
    private Long stationId;

    private String stationName;

    @NotNull(message = "Model xe không được để trống")
    private Long modelId;

    private String modelName;

    @NotNull(message = "Giá thuê theo giờ không được để trống")
    @Positive(message = "Giá thuê theo giờ phải là số dương")
    private Double pricePerHour;

    @NotNull(message = "Giá thuê theo ngày không được để trống")
    @Positive(message = "Giá thuê theo ngày phải là số dương")
    private Double pricePerDay;

    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]-[0-9]{4,5}$",
            message = "Biển số xe không đúng định dạng (VD: 51F-12345)"
    )
    private String plateNumber;

    @NotNull(message = "Mức pin hiện tại không được để trống")
    @DecimalMin(value = "0.0", message = "Mức pin phải >= 0%")
    @DecimalMax(value = "100.0", message = "Mức pin phải <= 100%")
    private Double batteryLevel;

    @NotNull(message = "Số km đã đi không được để trống")
    @PositiveOrZero(message = "Số km phải >= 0")
    private Double mileage;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @NotNull(message = "Trạng thái xe không được để trống")
    private Vehicle.Status status;
}
