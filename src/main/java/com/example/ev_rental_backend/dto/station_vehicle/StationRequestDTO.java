package com.example.ev_rental_backend.dto.station_vehicle;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StationRequestDTO {
    @NotBlank(message = "Tên trạm không được để trống")
    @Size(max = 200, message = "Tên trạm không được vượt quá 200 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    private String location;

    @NotNull(message = "Vĩ độ không được để trống")
    @DecimalMin(value = "-90.0", message = "Vĩ độ phải >= -90")
    @DecimalMax(value = "90.0", message = "Vĩ độ phải <= 90")
    private Double latitude;

    @NotNull(message = "Kinh độ không được để trống")
    @DecimalMin(value = "-180.0", message = "Kinh độ phải >= -180")
    @DecimalMax(value = "180.0", message = "Kinh độ phải <= 180")
    private Double longitude;

    @NotNull(message = "Số lượng xe tối đa không được để trống")
    @Min(value = 1, message = "Số lượng xe tối đa phải >= 1")
    @Max(value = 1000, message = "Số lượng xe tối đa phải <= 1000")
    private Integer carNumber;

    private String status;
}
