package com.example.ev_rental_backend.service.report;

import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;

import java.util.List;

public interface AdminReportService {
    public BookingResponseBlacklistDTO getBookingDetailForReport(Long bookingId);

    public List<RenterResponseDTO> getBlacklistedRenters();
}
