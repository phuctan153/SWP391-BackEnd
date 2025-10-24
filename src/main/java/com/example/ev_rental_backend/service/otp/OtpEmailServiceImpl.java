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

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpEmailServiceImpl implements OtpEmailService{

    private final JavaMailSender mailSender;
    private final RenterRepository renterRepository;
    private final OtpVerificationEmailRepository otpRepository;

    @Override
    public void sendOtpByEmail(String email) throws MessagingException {
        Renter renter = renterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với email: " + email));

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpVerificationEmail otpRecord = OtpVerificationEmail.builder()
                .renter(renter)
                .otpCode(otp)
                .build();

        otpRepository.save(otpRecord);

        sendOtpEmail(renter.getEmail(), otp);
    }

    @Override
    public boolean verifyOtpByEmail(String email, String otpCode) {
        Renter renter = renterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với email: " + email));

        OtpVerificationEmail otp = otpRepository.findLatestOtpByRenterId(renter.getRenterId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy OTP cho renter này"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpVerificationEmail.Status.EXPIRED);
            otpRepository.save(otp);
            throw new RuntimeException("OTP đã hết hạn");
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setStatus(OtpVerificationEmail.Status.UNVERIFIED);
            otpRepository.save(otp);
            throw new RuntimeException("Mã OTP không hợp lệ");
        }

        otp.setStatus(OtpVerificationEmail.Status.VERIFIED);
        otpRepository.save(otp);
        return true;
    }



    @Override
    public void sendOtpEmail(String to, String otpCode) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(to);
        helper.setSubject("EV Rental - Mã xác thực OTP");
        helper.setText("""
                <h3>Mã OTP của bạn là:</h3>
                <h2 style='color:blue;'>%s</h2>
                <p>OTP sẽ hết hạn sau 5 phút.</p>
                """.formatted(otpCode), true);
        mailSender.send(msg);
    }

    @Override
    public boolean isRenterVerified(Long renterId) {
        return otpRepository.existsVerifiedOtpForRenter(renterId);
    }
}
