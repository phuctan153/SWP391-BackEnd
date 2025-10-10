package com.example.ev_rental_backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponseDTO {
    private Long globalAdminId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
}
