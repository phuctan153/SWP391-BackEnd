package com.example.ev_rental_backend.dto.admin;

import com.example.ev_rental_backend.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResponseDto {

    private Long globalAdminId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Admin.Status status;
    private LocalDateTime createdAt;
    private Integer totalContracts; // Số hợp đồng đã ký
}
