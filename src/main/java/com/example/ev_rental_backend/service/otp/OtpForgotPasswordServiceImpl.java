package com.example.ev_rental_backend.service.otp;

import com.example.ev_rental_backend.dto.forgot_password.ResetPasswordWithOtpDTO;
import com.example.ev_rental_backend.entity.OtpVerificationEmail;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.OtpVerificationEmailRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpForgotPasswordServiceImpl implements OtpForgotPasswordService {

    private final OtpVerificationEmailRepository otpRepository;
    private final RenterRepository renterRepository;
    private final JavaMailSender mailSender;

    // 🔹 1️⃣ Gửi OTP đến email
    @Override
    public void sendForgotPasswordOtp(String email) throws MessagingException {
        Renter renter = renterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với email: " + email));

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpVerificationEmail otpEntity = OtpVerificationEmail.builder()
                .renter(renter)
                .otpCode(otp)
                .build();

        otpRepository.save(otpEntity);

        // Gửi email
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(email);
        helper.setSubject("EV Rental - Xác nhận quên mật khẩu");
        helper.setText("""
            <h3>Xin chào %s,</h3>
            <p>Mã OTP khôi phục mật khẩu của bạn là:</p>
            <h2 style='color:blue;'>%s</h2>
            <p>Mã OTP này có hiệu lực trong vòng <b>5 phút</b>.</p>
            """.formatted(renter.getFullName(), otp), true);

        mailSender.send(msg);
    }

    // 🔹 2️⃣ Xác thực OTP và đổi mật khẩu
    @Override
    public void verifyOtpAndResetPassword(ResetPasswordWithOtpDTO dto) {
        Renter renter = renterRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với email này"));

        OtpVerificationEmail otp = otpRepository.findLatestOtpByRenterId(renter.getRenterId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy OTP cho renter này"));

        // Hết hạn
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpVerificationEmail.Status.EXPIRED);
            otpRepository.save(otp);
            throw new RuntimeException("OTP đã hết hạn");
        }

        // Sai mã OTP
        if (!otp.getOtpCode().equals(dto.getOtpCode())) {
            otp.setStatus(OtpVerificationEmail.Status.UNVERIFIED);
            otpRepository.save(otp);
            throw new RuntimeException("Mã OTP không hợp lệ");
        }

        // Mật khẩu không trùng khớp
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // ✅ Mã hóa mật khẩu mới
        renter.setPassword(dto.getPassword());
        renterRepository.save(renter);

        // ✅ Đánh dấu OTP đã dùng
        otp.setStatus(OtpVerificationEmail.Status.VERIFIED);
        otpRepository.save(otp);
    }
}
