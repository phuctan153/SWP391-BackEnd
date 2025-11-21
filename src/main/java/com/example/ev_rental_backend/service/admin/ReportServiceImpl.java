package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.dto.report.*;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RenterRepository renterRepository;
    private final IdentityDocumentRepository identityDocumentRepository;

    /**
     * Báo cáo doanh thu
     */
    public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate, String groupBy) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Lấy tất cả invoice đã thanh toán trong khoảng thời gian
        List<Invoice> invoices = invoiceRepository.findByCompletedAtBetween(startDateTime, endDateTime);

        // Tính tổng doanh thu
        Double totalRevenue = invoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        // Tính tổng cọc
        Double totalDeposit = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.Type.DEPOSIT)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        // Tính tổng đã hoàn cọc
        Double totalRefunded = invoices.stream()
                .filter(inv -> inv.getType() == Invoice.Type.FINAL)
                .mapToDouble(inv -> inv.getDepositAmount() != null ? inv.getDepositAmount() : 0.0)
                .sum();

        // Net revenue
        Double netRevenue = totalRevenue - totalRefunded;

        // Doanh thu theo phương thức thanh toán
        Double cashRevenue = calculateRevenueByPaymentMethod(invoices, Invoice.PaymentMethod.CASH);
        Double walletRevenue = calculateRevenueByPaymentMethod(invoices, Invoice.PaymentMethod.WALLET);
        Double momoRevenue = calculateRevenueByPaymentMethod(invoices, Invoice.PaymentMethod.MOMO);

        // Doanh thu theo thời gian
        List<RevenueByDateDto> revenueByDate = calculateRevenueByDate(invoices, groupBy);

        // Top vehicles
        List<VehicleRevenueDto> topVehicles = calculateTopVehicles(startDateTime, endDateTime);

        return RevenueReportDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .groupBy(groupBy)
                .totalRevenue(totalRevenue)
                .totalDeposit(totalDeposit)
                .totalRefunded(totalRefunded)
                .netRevenue(netRevenue)
                .cashRevenue(cashRevenue)
                .walletRevenue(walletRevenue)
                .momoRevenue(momoRevenue)
                .revenueByDate(revenueByDate)
                .topVehicles(topVehicles)
                .build();
    }

    /**
     * Thống kê booking
     */
    public BookingReportDto getBookingReport(LocalDate startDate, LocalDate endDate, String status) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            List<Booking> bookings = bookingRepository.findByCreatedAtBetween(startDateTime, endDateTime);
            if (bookings == null) {
                bookings = new ArrayList<>();
            }

            // Filter theo status nếu có
            if (status != null && !status.isEmpty()) {
                try {
                    Booking.Status bookingStatus = Booking.Status.valueOf(status.toUpperCase());
                    bookings = bookings.stream()
                            .filter(b -> b.getStatus() == bookingStatus)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status provided: {}", status);
                }
            }

            Integer totalBookings = bookings.size();

            // Đếm theo trạng thái
            Integer pendingBookings = countByStatus(bookings, Booking.Status.PENDING);
            Integer reservedBookings = countByStatus(bookings, Booking.Status.RESERVED);
            Integer inUseBookings = countByStatus(bookings, Booking.Status.IN_USE);
            Integer completedBookings = countByStatus(bookings, Booking.Status.COMPLETED);
            Integer cancelledBookings = countByStatus(bookings, Booking.Status.CANCELLED);
            Integer expiredBookings = countByStatus(bookings, Booking.Status.EXPIRED);

            // Tỷ lệ
            Double completionRate = totalBookings > 0 ? (completedBookings * 100.0 / totalBookings) : 0.0;
            Double cancellationRate = totalBookings > 0 ? (cancelledBookings * 100.0 / totalBookings) : 0.0;

            // Booking theo ngày
            List<BookingByDateDto> bookingsByDate = calculateBookingsByDate(bookings);

            // Top renters
            List<RenterBookingDto> topRenters = calculateTopRenters(bookings);

            return BookingReportDto.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalBookings(totalBookings)
                    .pendingBookings(pendingBookings)
                    .reservedBookings(reservedBookings)
                    .inUseBookings(inUseBookings)
                    .completedBookings(completedBookings)
                    .cancelledBookings(cancelledBookings)
                    .expiredBookings(expiredBookings)
                    .completionRate(completionRate)
                    .cancellationRate(cancellationRate)
                    .bookingsByDate(bookingsByDate)
                    .topRenters(topRenters)
                    .build();
        } catch (Exception e) {
            log.error("Error in getBookingReport: ", e);
            throw e; // cho Spring in stack trace
        }
    }

    /**
     * Thống kê xe
     */
    public VehicleReportDto getVehicleReport(Long stationId) {
        List<Vehicle> vehicles;

        if (stationId != null) {
            vehicles = vehicleRepository.findByStation_StationId(stationId);
        } else {
            vehicles = vehicleRepository.findAll();
        }

        Integer totalVehicles = vehicles.size();
        Integer availableVehicles = countByStatus(vehicles, Vehicle.Status.AVAILABLE);
        Integer inUseVehicles = countByStatus(vehicles, Vehicle.Status.IN_USE);
        Integer maintenanceVehicles = countByStatus(vehicles, Vehicle.Status.MAINTENANCE);
        Integer inRepairVehicles = countByStatus(vehicles, Vehicle.Status.IN_REPAIR);

        Double utilizationRate = totalVehicles > 0 ? (inUseVehicles * 100.0 / totalVehicles) : 0.0;

        // Xe theo trạm
        List<VehicleByStationDto> vehiclesByStation = calculateVehiclesByStation();

        // Xe theo model
        Map<String, Integer> vehiclesByModel = vehicles.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getModel().getModelName(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Top vehicles
        List<TopVehicleDto> topVehicles = calculateTopBookedVehicles();

        // Xe cần bảo trì
        List<MaintenanceNeededDto> maintenanceNeeded = findVehiclesNeedMaintenance(vehicles);

        return VehicleReportDto.builder()
                .totalVehicles(totalVehicles)
                .availableVehicles(availableVehicles)
                .inUseVehicles(inUseVehicles)
                .maintenanceVehicles(maintenanceVehicles)
                .inRepairVehicles(inRepairVehicles)
                .utilizationRate(utilizationRate)
                .vehiclesByStation(vehiclesByStation)
                .vehiclesByModel(vehiclesByModel)
                .topVehicles(topVehicles)
                .maintenanceNeeded(maintenanceNeeded)
                .build();
    }

    /**
     * Thống kê trạm
     */
    public StationReportDto getStationReport() {
        List<Station> stations = stationRepository.findAll();

        Integer totalStations = stations.size();
        Integer activeStations = (int) stations.stream()
                .filter(s -> s.getStatus() == Station.Status.ACTIVE)
                .count();
        Integer inactiveStations = totalStations - activeStations;

        // Chi tiết từng trạm
        List<StationDetailDto> stationDetails = stations.stream()
                .map(this::mapToStationDetailDto)
                .collect(Collectors.toList());

        // Top stations
        List<TopStationDto> topStations = calculateTopStations(stationDetails);

        return StationReportDto.builder()
                .totalStations(totalStations)
                .activeStations(activeStations)
                .inactiveStations(inactiveStations)
                .stations(stationDetails)
                .topStations(topStations)
                .build();
    }

    /**
     * Dashboard tổng quan
     */
    public DashboardReportDto getDashboardReport() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        // Today stats
        List<Booking> todayBookings = bookingRepository.findByCreatedAtBetween(
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );
        Integer totalBookingsToday = todayBookings.size();

        List<Invoice> todayInvoices = invoiceRepository.findByCompletedAtBetween(
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );
        Double revenueToday = todayInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        // This month stats
        List<Booking> thisMonthBookings = bookingRepository.findByCreatedAtBetween(
                startOfMonth.atStartOfDay(),
                today.atTime(23, 59, 59)
        );
        Integer bookingsThisMonth = thisMonthBookings.size();

        List<Invoice> thisMonthInvoices = invoiceRepository.findByCompletedAtBetween(
                startOfMonth.atStartOfDay(),
                today.atTime(23, 59, 59)
        );
        Double revenueThisMonth = thisMonthInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();

        // Active renters (verified)
        Integer activeRenters = (int) renterRepository.findByStatus(Renter.Status.VERIFIED).size();

        // New renters this month
        Integer newRentersThisMonth = (int) renterRepository.findByCreatedAtBetween(
                startOfMonth.atStartOfDay(),
                today.atTime(23, 59, 59)
        ).size();

        // Total vehicles
        Integer totalVehicles = (int) vehicleRepository.count();

        // Growth calculations
        List<Booking> lastMonthBookings = bookingRepository.findByCreatedAtBetween(
                startOfLastMonth.atStartOfDay(),
                endOfLastMonth.atTime(23, 59, 59)
        );
        Double bookingGrowth = calculateGrowth(lastMonthBookings.size(), bookingsThisMonth);

        List<Invoice> lastMonthInvoices = invoiceRepository.findByCompletedAtBetween(
                startOfLastMonth.atStartOfDay(),
                endOfLastMonth.atTime(23, 59, 59)
        );
        Double lastMonthRevenue = lastMonthInvoices.stream()
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
        Double revenueGrowth = calculateGrowth(lastMonthRevenue, revenueThisMonth);

        // Quick stats
        Integer pendingVerifications = (int) identityDocumentRepository
                .findByStatus(IdentityDocument.DocumentStatus.PENDING).size();

        List<Vehicle> allVehicles = vehicleRepository.findAll();
        Integer vehiclesNeedMaintenance = (int) allVehicles.stream()
                .filter(v -> v.getMileage() != null && v.getMileage() > 10000)
                .count();

        LocalDateTime now = LocalDateTime.now();
        Integer expiringSoonBookings = (int) bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == Booking.Status.RESERVED)
                .filter(b -> b.getExpiresAt() != null && b.getExpiresAt().isBefore(now.plusHours(2)))
                .count();

        // Recent activities (last 10)
        List<RecentActivityDto> recentActivities = generateRecentActivities();

        return DashboardReportDto.builder()
                .totalBookingsToday(totalBookingsToday)
                .revenueToday(revenueToday)
                .activeRenters(activeRenters)
                .totalVehicles(totalVehicles)
                .bookingsThisMonth(bookingsThisMonth)
                .revenueThisMonth(revenueThisMonth)
                .newRentersThisMonth(newRentersThisMonth)
                .bookingGrowth(bookingGrowth)
                .revenueGrowth(revenueGrowth)
                .pendingVerifications(pendingVerifications)
                .vehiclesNeedMaintenance(vehiclesNeedMaintenance)
                .expiringSoonBookings(expiringSoonBookings)
                .recentActivities(recentActivities)
                .build();
    }

    // ==================== Helper Methods ====================

    private Double calculateRevenueByPaymentMethod(List<Invoice> invoices, Invoice.PaymentMethod method) {
        return invoices.stream()
                .filter(inv -> inv.getPaymentMethod() == method)
                .mapToDouble(Invoice::getTotalAmount)
                .sum();
    }

    private List<RevenueByDateDto> calculateRevenueByDate(List<Invoice> invoices, String groupBy) {
        DateTimeFormatter formatter;
        switch (groupBy.toUpperCase()) {
            case "MONTH":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "YEAR":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            default: // DAY
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        Map<String, List<Invoice>> grouped = invoices.stream()
                .filter(inv -> inv.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        inv -> inv.getCompletedAt().format(formatter)
                ));

        return grouped.entrySet().stream()
                .map(entry -> RevenueByDateDto.builder()
                        .date(entry.getKey())
                        .revenue(entry.getValue().stream()
                                .mapToDouble(Invoice::getTotalAmount)
                                .sum())
                        .bookingCount(entry.getValue().size())
                        .build())
                .sorted(Comparator.comparing(RevenueByDateDto::getDate))
                .collect(Collectors.toList());
    }

    private List<VehicleRevenueDto> calculateTopVehicles(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Booking> bookings = bookingRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        Map<Vehicle, List<Booking>> grouped = bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.COMPLETED)
                .collect(Collectors.groupingBy(Booking::getVehicle));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Vehicle vehicle = entry.getKey();
                    List<Booking> vehicleBookings = entry.getValue();

                    Double revenue = vehicleBookings.stream()
                            .mapToDouble(Booking::getTotalAmount)
                            .sum();

                    return VehicleRevenueDto.builder()
                            .vehicleId(vehicle.getVehicleId())
                            .vehicleName(vehicle.getVehicleName())
                            .plateNumber(vehicle.getPlateNumber())
                            .revenue(revenue)
                            .bookingCount(vehicleBookings.size())
                            .build();
                })
                .sorted(Comparator.comparing(VehicleRevenueDto::getRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private Integer countByStatus(List<Booking> bookings, Booking.Status status) {
        return (int) bookings.stream()
                .filter(b -> b.getStatus() == status)
                .count();
    }

    private Integer countByStatus(List<Vehicle> vehicles, Vehicle.Status status) {
        return (int) vehicles.stream()
                .filter(v -> v.getStatus() == status)
                .count();
    }

    private List<BookingByDateDto> calculateBookingsByDate(List<Booking> bookings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> grouped = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getCreatedAt().format(formatter),
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .map(entry -> BookingByDateDto.builder()
                        .date(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .sorted(Comparator.comparing(BookingByDateDto::getDate))
                .collect(Collectors.toList());
    }

    private List<RenterBookingDto> calculateTopRenters(List<Booking> bookings) {
        Map<Renter, List<Booking>> grouped = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getRenter));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Renter renter = entry.getKey();
                    List<Booking> renterBookings = entry.getValue();

                    Double totalSpent = renterBookings.stream()
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                            .sum();

                    return RenterBookingDto.builder()
                            .renterId(renter.getRenterId())
                            .renterName(renter.getFullName())
                            .email(renter.getEmail())
                            .bookingCount(renterBookings.size())
                            .totalSpent(totalSpent)
                            .build();
                })
                .sorted(Comparator.comparing(RenterBookingDto::getBookingCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<VehicleByStationDto> calculateVehiclesByStation() {
        List<Station> stations = stationRepository.findAll();

        return stations.stream()
                .map(station -> {
                    List<Vehicle> stationVehicles = vehicleRepository.findByStation_StationId(station.getStationId());

                    return VehicleByStationDto.builder()
                            .stationId(station.getStationId())
                            .stationName(station.getName())
                            .totalVehicles(stationVehicles.size())
                            .availableVehicles((int) stationVehicles.stream()
                                    .filter(v -> v.getStatus() == Vehicle.Status.AVAILABLE)
                                    .count())
                            .inUseVehicles((int) stationVehicles.stream()
                                    .filter(v -> v.getStatus() == Vehicle.Status.IN_USE)
                                    .count())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TopVehicleDto> calculateTopBookedVehicles() {
        List<Booking> allBookings = bookingRepository.findAll();

        Map<Vehicle, List<Booking>> grouped = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.COMPLETED)
                .collect(Collectors.groupingBy(Booking::getVehicle));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Vehicle vehicle = entry.getKey();
                    List<Booking> bookings = entry.getValue();

                    Double revenue = bookings.stream()
                            .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                            .sum();

                    // Calculate average rating (if exists)
                    Double averageRating = bookings.stream()
                            .filter(b -> b.getBookingRating() != null)
                            .mapToDouble(b -> b.getBookingRating().getVehicleRating())
                            .average()
                            .orElse(0.0);

                    return TopVehicleDto.builder()
                            .vehicleId(vehicle.getVehicleId())
                            .vehicleName(vehicle.getVehicleName())
                            .plateNumber(vehicle.getPlateNumber())
                            .bookingCount(bookings.size())
                            .averageRating(averageRating)
                            .totalRevenue(revenue)
                            .build();
                })
                .sorted(Comparator.comparing(TopVehicleDto::getBookingCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<MaintenanceNeededDto> findVehiclesNeedMaintenance(List<Vehicle> vehicles) {
        return vehicles.stream()
                .filter(v -> v.getMileage() != null && v.getMileage() > 10000)
                .map(v -> MaintenanceNeededDto.builder()
                        .vehicleId(v.getVehicleId())
                        .vehicleName(v.getVehicleName())
                        .plateNumber(v.getPlateNumber())
                        .mileage(v.getMileage())
                        .reason("Mileage exceeds 10,000 km")
                        .build())
                .collect(Collectors.toList());
    }

    private StationDetailDto mapToStationDetailDto(Station station) {
        List<Vehicle> vehicles = vehicleRepository.findByStation_StationId(station.getStationId());
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getVehicle().getStation().getStationId().equals(station.getStationId()))
                .collect(Collectors.toList());

        Double revenue = bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.COMPLETED)
                .mapToDouble(b -> b.getTotalAmount() != null ? b.getTotalAmount() : 0.0)
                .sum();

        return StationDetailDto.builder()
                .stationId(station.getStationId())
                .name(station.getName())
                .location(station.getLocation())
                .status(station.getStatus().name())
                .totalVehicles(vehicles.size())
                .availableVehicles((int) vehicles.stream()
                        .filter(v -> v.getStatus() == Vehicle.Status.AVAILABLE)
                        .count())
                .totalBookings(bookings.size())
                .revenue(revenue)
                .staffCount(station.getStaffStations() != null ? station.getStaffStations().size() : 0)
                .build();
    }

    private List<TopStationDto> calculateTopStations(List<StationDetailDto> stations) {
        return stations.stream()
                .map(s -> {
                    Double utilizationRate = s.getTotalVehicles() > 0 ?
                            ((s.getTotalVehicles() - s.getAvailableVehicles()) * 100.0 / s.getTotalVehicles()) : 0.0;

                    return TopStationDto.builder()
                            .stationId(s.getStationId())
                            .name(s.getName())
                            .bookingCount(s.getTotalBookings())
                            .revenue(s.getRevenue())
                            .utilizationRate(utilizationRate)
                            .build();
                })
                .sorted(Comparator.comparing(TopStationDto::getRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private Double calculateGrowth(Number previous, Number current) {
        double prev = previous.doubleValue();
        double curr = current.doubleValue();

        if (prev == 0) {
            return curr > 0 ? 100.0 : 0.0;
        }

        return ((curr - prev) / prev) * 100.0;
    }

    private List<RecentActivityDto> generateRecentActivities() {
        List<RecentActivityDto> activities = new ArrayList<>();

        // Get recent bookings
        List<Booking> recentBookings = bookingRepository.findAll().stream()
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (Booking booking : recentBookings) {
            activities.add(RecentActivityDto.builder()
                    .type("BOOKING_CREATED")
                    .description(String.format("New booking #%d by %s",
                            booking.getBookingId(),
                            booking.getRenter().getFullName()))
                    .timestamp(booking.getCreatedAt())
                    .build());
        }

        // Sort by timestamp
        activities.sort(Comparator.comparing(RecentActivityDto::getTimestamp).reversed());

        return activities.stream().limit(10).collect(Collectors.toList());
    }
}
