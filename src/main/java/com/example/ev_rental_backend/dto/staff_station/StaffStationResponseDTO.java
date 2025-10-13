package com.example.ev_rental_backend.dto.staff_station;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffStationResponseDTO {
    private Long staffStationId;
    private Long staffId;
    private Long stationId;
    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;
    private String roleAtStation;
    private String status;
}
