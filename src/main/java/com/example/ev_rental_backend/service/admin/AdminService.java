package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.entity.Admin;

public interface AdminService {
    Admin loginAdmin(String email, String password);
}
