package com.example.ev_rental_backend.service.report;

import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.mapper.BookingMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    // ✅ Lấy chi tiết 1 booking (cho admin/staff xem report)
    public BookingResponseBlacklistDTO getBookingDetailForReport(Long bookingId) {
        Booking booking = bookingRepository.findBookingWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID = " + bookingId));

        // Mapping sang DTO trả về
        return bookingMapper.toBlacklistDto(booking);
    }
}
