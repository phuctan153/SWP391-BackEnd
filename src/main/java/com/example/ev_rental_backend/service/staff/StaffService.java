package com.example.ev_rental_backend.service.staff;

import com.example.ev_rental_backend.dto.staff.StaffDetailDTO;
import com.example.ev_rental_backend.dto.staff.StaffListDTO;
import com.example.ev_rental_backend.entity.Staff;

import java.util.List;

public interface StaffService {
    public Staff loginStaff(String email, String password);

    public List<StaffListDTO> getStaffByStation(Long stationId);
    public StaffDetailDTO getStaffDetail(Long staffId);
}
