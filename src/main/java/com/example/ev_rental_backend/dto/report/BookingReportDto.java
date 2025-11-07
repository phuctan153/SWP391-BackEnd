package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReportDto {

    private LocalDate startDate;
    private LocalDate endDate;

    // Tổng số booking
    private Integer totalBookings;

    // Theo trạng thái
    private Integer pendingBookings;
    private Integer reservedBookings;
    private Integer inUseBookings;
    private Integer completedBookings;
    private Integer cancelledBookings;
    private Integer expiredBookings;

    // Tỷ lệ
    private Double completionRate; // completed / total * 100
    private Double cancellationRate; // cancelled / total * 100

    // Theo thời gian
    private List<BookingByDateDto> bookingsByDate;

    // Top renters
    private List<RenterBookingDto> topRenters;
}
