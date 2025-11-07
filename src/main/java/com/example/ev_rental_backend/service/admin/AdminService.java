package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.dto.admin.AdminResponseDto;
import com.example.ev_rental_backend.dto.admin.CreateAdminDto;
import com.example.ev_rental_backend.dto.admin.UpdateAdminDto;
import com.example.ev_rental_backend.entity.Admin;

import java.util.List;

public interface AdminService {
    Admin loginAdmin(String email, String password);

    public AdminResponseDto getCurrentAdmin();
    public AdminResponseDto updateCurrentAdmin(UpdateAdminDto requestDto);
    public List<AdminResponseDto> getAllAdmins();
    public AdminResponseDto createAdmin(CreateAdminDto requestDto);
    public AdminResponseDto updateAdmin(Long adminId, UpdateAdminDto requestDto);
}
