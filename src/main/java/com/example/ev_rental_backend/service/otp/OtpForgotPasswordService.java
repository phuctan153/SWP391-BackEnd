package com.example.ev_rental_backend.service.otp;

import com.example.ev_rental_backend.dto.forgot_password.ResetPasswordWithOtpDTO;
import jakarta.mail.MessagingException;

public interface OtpForgotPasswordService {
    public void sendForgotPasswordOtp(String email) throws MessagingException;

    public void verifyOtpAndResetPassword(ResetPasswordWithOtpDTO dto);
}
