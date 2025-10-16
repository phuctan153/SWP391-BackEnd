package com.example.ev_rental_backend.service.otp;

import jakarta.mail.MessagingException;

public interface OtpForgotPasswordService {
    void sendForgotPasswordOtp(String email) throws MessagingException;
}
