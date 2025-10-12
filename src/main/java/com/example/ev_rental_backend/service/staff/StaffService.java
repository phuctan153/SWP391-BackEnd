package com.example.ev_rental_backend.service.staff;

import com.example.ev_rental_backend.entity.Staff;

public interface StaffService {
    public Staff loginStaff(String email, String password);
}
