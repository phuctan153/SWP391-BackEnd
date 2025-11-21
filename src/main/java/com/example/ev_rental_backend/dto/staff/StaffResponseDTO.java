package com.example.ev_rental_backend.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponseDTO {
    private Long staffId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;
}
