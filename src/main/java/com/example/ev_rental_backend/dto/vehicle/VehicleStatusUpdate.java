package com.example.ev_rental_backend.dto.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleStatusUpdate {
    @NotBlank(message = "Trạng thái xe không được để trống")
    @Pattern(
            regexp = "^(AVAILABLE|RESERVED|IN_USE|MAINTENANCE)$",
            message = "Trạng thái xe phải là: AVAILABLE, RESERVED, IN_USE, MAINTENANCE"
    )
    private String status;
}
