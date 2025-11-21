package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.dto.report.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface ReportService {
    public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate, String groupBy);
    public BookingReportDto getBookingReport(LocalDate startDate, LocalDate endDate, String status);
    public VehicleReportDto getVehicleReport(Long stationId);
    public StationReportDto getStationReport();
    public DashboardReportDto getDashboardReport();
}
