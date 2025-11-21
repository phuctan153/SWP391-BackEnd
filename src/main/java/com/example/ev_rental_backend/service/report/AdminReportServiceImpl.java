package com.example.ev_rental_backend.service.report;

import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.mapper.BookingMapper;
import com.example.ev_rental_backend.mapper.RenterMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RenterMapper renterMapper;
    private final RenterRepository renterRepository;

    // ✅ Lấy chi tiết 1 booking (cho admin/staff xem report)
    public BookingResponseBlacklistDTO getBookingDetailForReport(Long bookingId) {
        Booking booking = bookingRepository.findBookingWithDetails(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID = " + bookingId));

        // Mapping sang DTO trả về
        return bookingMapper.toBlacklistDto(booking);
    }

    @Override
    public List<RenterResponseDTO> getBlacklistedRenters() {
        List<Renter> renters = renterRepository.findByIsBlacklistedTrue();

        return renters.stream()
                .map(renterMapper::toResponseDto)
                .toList();
    }
}
