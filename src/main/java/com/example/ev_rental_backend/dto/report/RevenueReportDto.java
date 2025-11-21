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
public class RevenueReportDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private String groupBy; // DAY, MONTH, YEAR

    // Tổng quan
    private Double totalRevenue;
    private Double totalDeposit;
    private Double totalRefunded;
    private Double netRevenue; // totalRevenue - totalRefunded

    // Theo phương thức thanh toán
    private Double cashRevenue;
    private Double walletRevenue;
    private Double momoRevenue;

    // Chi tiết theo thời gian
    private List<RevenueByDateDto> revenueByDate;

    // Top vehicles revenue
    private List<VehicleRevenueDto> topVehicles;
}
