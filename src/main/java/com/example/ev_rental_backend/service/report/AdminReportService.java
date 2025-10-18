package com.example.ev_rental_backend.service.report;

import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;

public interface AdminReportService {
    public BookingResponseBlacklistDTO getBookingDetailForReport(Long bookingId);
}
