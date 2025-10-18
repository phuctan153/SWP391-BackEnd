package com.example.ev_rental_backend.service.warning;

import com.example.ev_rental_backend.dto.warning.WarningRequestDTO;
import com.example.ev_rental_backend.dto.warning.WarningResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.mapper.WarningMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarningServiceImpl implements WarningService {

    private final BookingRepository bookingRepository;
    private final WarningMapper warningMapper;
    private final JavaMailSender mailSender;

    public WarningResponseDTO sendWarningEmail(WarningRequestDTO dto) {

        // 🔹 1. Lấy booking & renter
        Booking booking = bookingRepository.findBookingWithDetails(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        Renter renter = booking.getRenter();
        if (renter.getEmail() == null || renter.getEmail().isEmpty()) {
            throw new RuntimeException("Renter không có email hợp lệ để gửi cảnh cáo");
        }

        // 🔹 2. Soạn email HTML
        String subject = "⚠️ Cảnh cáo vi phạm từ EV Rental System";
        String content = """
                <html>
                <body>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Hệ thống EV Rental ghi nhận rằng trong đơn thuê xe <b>#%d</b> 
                    (xe: %s - biển số %s) có phát sinh hư hỏng.</p>

                    <p><b>Ghi chú từ quản trị viên:</b></p>
                    <blockquote style='color:#d9534f;'>%s</blockquote>

                    <p>Xin lưu ý rằng nếu vi phạm lặp lại, tài khoản của bạn có thể bị đưa vào 
                    danh sách <b>Black List</b> và tạm khóa trong vòng <b>6 tháng</b>.</p>

                    <p>Trân trọng,<br/>EV Rental System Team</p>
                </body>
                </html>
                """.formatted(
                renter.getFullName(),
                booking.getBookingId(),
                booking.getVehicle().getModel(),
                booking.getVehicle().getPlateNumber(),
                dto.getNote()
        );

        // 🔹 3. Gửi email
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

        // 🔹 4. Trả về kết quả
        return warningMapper.toResponseDTO(booking, "Cảnh cáo đã được gửi tới " + renter.getEmail());
    }
}
