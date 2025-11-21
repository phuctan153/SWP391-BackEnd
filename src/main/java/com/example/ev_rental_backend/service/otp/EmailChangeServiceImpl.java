package com.example.ev_rental_backend.service.otp;
import com.example.ev_rental_backend.entity.OtpVerificationEmail;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.OtpVerificationEmailRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService{

    private final RenterRepository renterRepository;
    private final OtpVerificationEmailRepository otpRepository;
    private final JavaMailSender mailSender;


    @Override
    public void requestEmailChange(Long renterId, String newEmail) {
        // 1️⃣ Kiểm tra xem email mới đã được ai sử dụng chưa
        if (renterRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        // 2️⃣ Tìm renter theo ID
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê với ID: " + renterId));

        // 3️⃣ Sinh OTP ngẫu nhiên (6 chữ số)
        String otp = String.format("%06d", new Random().nextInt(999999));

        // 4️⃣ Tạo bản ghi OTP
        OtpVerificationEmail otpEntity = OtpVerificationEmail.builder()
                .renter(renter)
                .otpCode(otp)
                .status(OtpVerificationEmail.Status.UNVERIFIED)
                .build();

        otpRepository.save(otpEntity);

        // 5️⃣ Gửi email xác nhận
        sendVerificationEmail(newEmail, otp);
    }

    private void sendVerificationEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("🔐 Xác nhận thay đổi email - EV Rental");
            helper.setText("""
                    <p>Xin chào,</p>
                    <p>Mã OTP để xác nhận thay đổi email của bạn là:</p>
                    <h2>%s</h2>
                    <p>Mã này có hiệu lực trong 10 phút.</p>
                    <br/>
                    <p>Trân trọng,<br>EV Rental System</p>
                    """.formatted(otp), true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email xác nhận: " + e.getMessage());
        }
    }

    @Override
    public void confirmEmailChange(Long renterId, String otpCode, String newEmail) {
        // 1️⃣ Tìm renter
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với ID: " + renterId));

        // 2️⃣ Lấy OTP mới nhất của renter
        OtpVerificationEmail latestOtp = otpRepository.findLatestOtpByRenterId(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP nào cho renter này."));

        // 3️⃣ Kiểm tra OTP còn hiệu lực không
        if (latestOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            latestOtp.setStatus(OtpVerificationEmail.Status.EXPIRED);
            otpRepository.save(latestOtp);
            throw new RuntimeException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        // 4️⃣ Kiểm tra mã OTP có trùng khớp không
        if (!latestOtp.getOtpCode().equals(otpCode)) {
            throw new RuntimeException("❌ Mã OTP không chính xác!");
        }

        latestOtp.setStatus(OtpVerificationEmail.Status.VERIFIED);
        otpRepository.save(latestOtp);

        renter.setEmail(newEmail);
        renterRepository.save(renter);

    }
}
