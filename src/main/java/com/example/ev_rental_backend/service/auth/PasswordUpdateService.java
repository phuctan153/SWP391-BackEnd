package com.example.ev_rental_backend.service.auth;

import com.example.ev_rental_backend.dto.update_password.UpdatePasswordRequest;

public interface PasswordUpdateService {
    void updatePassword(Long userId, String role, UpdatePasswordRequest request);
}
