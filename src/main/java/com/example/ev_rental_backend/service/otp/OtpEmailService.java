package com.example.ev_rental_backend.service.otp;

import jakarta.mail.MessagingException;

public interface OtpEmailService {
    public void sendOtp(Long renterId) throws MessagingException;

    public boolean verifyOtp(Long renterId, String otpCode);

    void sendOtpEmail(String to, String otpCode) throws MessagingException;

    public boolean isRenterVerified(Long renterId);
}
