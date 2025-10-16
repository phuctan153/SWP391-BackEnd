package com.example.ev_rental_backend.service.otp;

import com.example.ev_rental_backend.entity.OtpVerificationEmail;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.OtpVerificationEmailRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpForgotPasswordServiceImpl implements OtpForgotPasswordService{


    private final JavaMailSender mailSender;
    private final RenterRepository renterRepository;
    private final OtpVerificationEmailRepository otpRepository;
    @Override
    public void sendForgotPasswordOtp(String email) throws MessagingException {
        Renter renter = renterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        String otpCode = String.format("%06d", new Random().nextInt(999999));

        OtpVerificationEmail otp = OtpVerificationEmail.builder()
                .renter(renter)
                .otpCode(otpCode)
                .build();
        otpRepository.save(otp);

        sendEmailOtp(email, otpCode);
    }

    private void sendEmailOtp(String to, String otpCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("EV Rental - Mã OTP khôi phục mật khẩu");
        helper.setText("""
                <h3>Xin chào!</h3>
                <p>Mã OTP khôi phục mật khẩu của bạn là:</p>
                <h2 style='color:blue;'>%s</h2>
                <p>OTP sẽ hết hạn sau 5 phút.</p>
                """.formatted(otpCode), true);
        mailSender.send(message);
    }
}
