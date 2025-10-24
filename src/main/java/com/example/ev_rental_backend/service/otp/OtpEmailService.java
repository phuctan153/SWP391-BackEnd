package com.example.ev_rental_backend.service.otp;

import jakarta.mail.MessagingException;

public interface OtpEmailService {

    public void sendOtpByEmail(String email) throws MessagingException;

    public boolean verifyOtpByEmail(String email, String otpCode);

    void sendOtpEmail(String to, String otpCode) throws MessagingException;

    public boolean isRenterVerified(Long renterId);
}
