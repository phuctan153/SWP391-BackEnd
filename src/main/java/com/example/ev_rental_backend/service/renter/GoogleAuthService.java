package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.login.LoginResponseDTO;

public interface GoogleAuthService {
    public ApiResponse<LoginResponseDTO> handleGoogleLogin(String sub, String email, String name, String picture);
}
