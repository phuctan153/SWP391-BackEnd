package com.example.ev_rental_backend.service.warning;

import com.example.ev_rental_backend.dto.warning.WarningRequestDTO;
import com.example.ev_rental_backend.dto.warning.WarningResponseDTO;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.mapper.WarningMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.RiskProfileRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.WalletRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final BookingRepository bookingRepository;
    private final RenterRepository renterRepository;
    private final RiskProfileRepository riskProfileRepository;
    private final WalletRepository walletRepository;
    private final WarningMapper warningMapper;
    private final JavaMailSender mailSender;

    @Override
    @Transactional
    public WarningResponseDTO sendWarningEmail(WarningRequestDTO dto) {

        // 🔹 1. Lấy booking & renter
        Booking booking = bookingRepository.findBookingWithDetails(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        Renter renter = booking.getRenter();
        if (renter.getEmail() == null || renter.getEmail().isEmpty()) {
            throw new RuntimeException("Renter không có email hợp lệ để gửi cảnh cáo");
        }

        // 🔹 2. Cập nhật RiskProfile
        RiskProfile profile = riskProfileRepository.findByRenter(renter)
                .orElseGet(() -> RiskProfile.builder()
                        .renter(renter)
                        .violationCount(0)
                        .riskLevel(RiskProfile.RiskLevel.LOW)
                        .lastViolationAt(LocalDateTime.now())
                        .build());

        profile.setViolationCount(profile.getViolationCount() + 1);
        profile.setLastViolationAt(LocalDateTime.now());
        profile.setNotes(dto.getNote());

        // Tự động nâng cấp mức độ rủi ro
        if (profile.getViolationCount() == 2)
            profile.setRiskLevel(RiskProfile.RiskLevel.MEDIUM);
        else if (profile.getViolationCount() >= 3)
            profile.setRiskLevel(RiskProfile.RiskLevel.HIGH);

        riskProfileRepository.save(profile);

        // 🔹 3. Nếu vi phạm ≥ 3 → đưa vào Blacklist
        if (profile.getViolationCount() >= 3) {
            renter.setBlacklisted(true);
            renter.setStatus(Renter.Status.DELETED);
            renterRepository.save(renter);

            // Khóa ví điện tử
            if (renter.getWallet() != null) {
                renter.getWallet().setStatus(Wallet.Status.INACTIVE);
                walletRepository.save(renter.getWallet());
            }
        }

        // 🔹 4. Soạn email HTML
        String subject = "⚠️ Cảnh cáo vi phạm từ EV Rental System";
        String content = """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
            <p>Xin chào <b>%s</b>,</p>

            <p>Hệ thống <b>EV Rental</b> ghi nhận rằng trong đơn thuê xe <b>#%d</b> 
            (xe: %s - biển số %s) đã có phát sinh <span style='color:#d9534f;'>hư hỏng</span> trong quá trình sử dụng.</p>

            <p><b>Ghi chú từ quản trị viên:</b></p>
            <blockquote style='border-left: 4px solid #d9534f; padding-left: 10px; color:#b52b27;'>
                %s
            </blockquote>

            <p>Hành vi này được xem là <b>vi phạm điều lệ trong hợp đồng thuê xe</b> mà bạn đã đồng ý khi xác nhận giao dịch. 
            Xin vui lòng đọc lại các điều khoản tại hợp đồng để tránh tái phạm.</p>

            <p>👉 <b>Lưu ý quan trọng:</b><br/>
            Bạn hiện đã có <b>%d</b> lần vi phạm.<br/>
            Nếu bạn vi phạm <b>3 lần</b>, tài khoản của bạn sẽ bị đưa vào
            <span style='color:#d9534f; font-weight:bold;'>Black List</span> và bị tạm khóa trong vòng <b>6 tháng</b>. 
            Trong thời gian đó, ví điện tử của bạn cũng sẽ bị vô hiệu hóa.</p>

            <p style='margin-top:20px;'>Trân trọng,<br/>
            <b>EV Rental System Team</b><br/>
            <small>Liên hệ hỗ trợ: support@evrental.com</small></p>
        </body>
        </html>
        """.formatted(
                renter.getFullName(),
                booking.getBookingId(),
                booking.getVehicle().getVehicleName(),
                booking.getVehicle().getPlateNumber(),
                dto.getNote(),
                profile.getViolationCount()
        );

        // 🔹 5. Gửi email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(renter.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email cảnh cáo: " + e.getMessage());
        }

        // 🔹 6. Trả về kết quả
        return warningMapper.toResponseDTO(booking, "Cảnh cáo đã được gửi tới " + renter.getEmail());
    }
}
