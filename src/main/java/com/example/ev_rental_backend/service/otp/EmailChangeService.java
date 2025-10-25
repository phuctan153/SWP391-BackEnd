package com.example.ev_rental_backend.service.otp;

public interface EmailChangeService {
    void requestEmailChange(Long renterId, String newEmail);
    void confirmEmailChange(Long renterId, String otpCode, String email);
}
