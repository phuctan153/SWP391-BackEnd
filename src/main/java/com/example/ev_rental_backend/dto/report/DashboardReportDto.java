package com.example.ev_rental_backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReportDto {

    // Overview numbers (hôm nay hoặc tháng này)
    private Integer totalBookingsToday;
    private Double revenueToday;
    private Integer activeRenters;
    private Integer totalVehicles;

    // This month
    private Integer bookingsThisMonth;
    private Double revenueThisMonth;
    private Integer newRentersThisMonth;

    // Trends (so với tháng trước)
    private Double bookingGrowth; // % tăng/giảm
    private Double revenueGrowth;

    // Quick stats
    private Integer pendingVerifications; // Renter chờ verify
    private Integer vehiclesNeedMaintenance;
    private Integer expiringSoonBookings; // Booking sắp hết hạn

    // Recent activities
    private List<RecentActivityDto> recentActivities;
}
