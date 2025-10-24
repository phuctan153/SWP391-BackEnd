package com.example.ev_rental_backend.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffDetailDTO {
    private Long staffId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private int totalCompleted;
    private double averageRating;
}
