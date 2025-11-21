package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.report.*;
import com.example.ev_rental_backend.service.admin.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    /**
     * GET /api/admin/reports/revenue
     * Báo cáo doanh thu
     *
     * Query params:
     * - startDate: ngày bắt đầu (yyyy-MM-dd)
     * - endDate: ngày kết thúc (yyyy-MM-dd)
     * - groupBy: DAY, MONTH, YEAR (optional)
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueReportDto>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "DAY") String groupBy) {

        RevenueReportDto report = reportService.getRevenueReport(startDate, endDate, groupBy);

        return ResponseEntity.ok(ApiResponse.<RevenueReportDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(report)
                .build());
    }

    /**
     * GET /api/admin/reports/bookings
     * Thống kê booking
     *
     * Query params:
     * - startDate: ngày bắt đầu (yyyy-MM-dd)
     * - endDate: ngày kết thúc (yyyy-MM-dd)
     * - status: RESERVED, IN_USE, COMPLETED, etc. (optional)
     */
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingReportDto>> getBookingReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {

        BookingReportDto report = reportService.getBookingReport(startDate, endDate, status);

        return ResponseEntity.ok(ApiResponse.<BookingReportDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(report)
                .build());
    }

    /**
     * GET /api/admin/reports/vehicles
     * Thống kê xe
     *
     * Query params:
     * - stationId: ID trạm (optional)
     */
    @GetMapping("/vehicles")
    public ResponseEntity<ApiResponse<VehicleReportDto>> getVehicleReport(
            @RequestParam(required = false) Long stationId) {

        VehicleReportDto report = reportService.getVehicleReport(stationId);

        return ResponseEntity.ok(ApiResponse.<VehicleReportDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(report)
                .build());
    }

    /**
     * GET /api/admin/reports/stations
     * Thống kê trạm
     */
    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<StationReportDto>> getStationReport() {
        StationReportDto report = reportService.getStationReport();

        return ResponseEntity.ok(ApiResponse.<StationReportDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(report)
                .build());
    }

    /**
     * GET /api/admin/reports/dashboard
     * Dashboard tổng quan (tất cả thống kê cơ bản)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardReportDto>> getDashboardReport() {
        DashboardReportDto report = reportService.getDashboardReport();

        return ResponseEntity.ok(ApiResponse.<DashboardReportDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(report)
                .build());
    }
}
