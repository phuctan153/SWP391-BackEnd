package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingBusinessRuleValidator {

    private final BookingRepository bookingRepository;

    /**
     * BR-05: Thời gian hợp lệ - Ngày, giờ bắt đầu và thời lượng thuê phải hợp lệ
     */
    public void validateBookingTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra startDateTime phải trong tương lai
        if (startDateTime.isBefore(now)) {
            throw new CustomException(
                    "Start date time must be in the future (BR-05)",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Kiểm tra endDateTime phải sau startDateTime
        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            throw new CustomException(
                    "End date time must be after start date time (BR-05)",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Tính thời lượng thuê (theo ngày)
        Duration duration = Duration.between(startDateTime, endDateTime);
        long days = duration.toDays();

        if (days < 1) {
            throw new CustomException(
                    "Rental duration must be at least 1 day (BR-05)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-05 validated: Booking time is valid for {} days", days);
    }

    /**
     * BR-06: Đặt xe kèm cọc - Việc giữ chỗ chỉ hợp lệ sau khi thanh toán tiền cọc
     */
    public void validateDepositPaid(Booking booking) {
        boolean hasDepositPaid = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.DEPOSIT
                        && inv.getStatus() == Invoice.Status.PAID);

        if (!hasDepositPaid) {
            log.error("BR-06 Violation: Booking {} has no paid deposit", booking.getBookingId());
            throw new CustomException(
                    "Deposit must be paid before pickup (BR-06)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-06 validated: Deposit paid for booking {}", booking.getBookingId());
    }

    /**
     * BR-07: Ưu tiên công bằng - Một xe không thể được nhiều khách giữ chỗ cùng lúc
     */
    public void validateVehicleAvailable(Vehicle vehicle, LocalDateTime startDateTime,
                                         LocalDateTime endDateTime) {

        if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
            throw new CustomException(
                    "Vehicle is not available (BR-07)",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Kiểm tra xem có booking nào overlap không
        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(vehicle.getVehicleId(), startDateTime, endDateTime);

        if (!overlappingBookings.isEmpty()) {
            log.error("BR-07 Violation: Vehicle {} has overlapping bookings",
                    vehicle.getVehicleId());
            throw new CustomException(
                    "Vehicle is already booked for the selected time period (BR-07)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-07 validated: Vehicle {} is available", vehicle.getVehicleId());
    }

    /**
     * BR-16: Giới hạn thuê 1 xe - Mỗi tài khoản chỉ được có 1 booking RESERVED hoặc IN_USE
     */
    public void validateRenterHasNoActiveBooking(Renter renter) {
        List<Booking> activeBookings = bookingRepository
                .findByRenterAndStatusIn(
                        renter,
                        List.of(Booking.Status.RESERVED, Booking.Status.IN_USE)
                );

        if (!activeBookings.isEmpty()) {
            log.error("BR-16 Violation: Renter {} has active booking(s)", renter.getRenterId());
            throw new CustomException(
                    "You already have an active booking. Please complete it before creating a new one (BR-16)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-16 validated: Renter {} has no active bookings", renter.getRenterId());
    }

    /**
     * BR-22: Giới hạn đặt trước - Thời gian đặt xe trước ngày sử dụng tối thiểu 7 ngày và tối đa 14 ngày
     */
    public void validateAdvanceBookingTime(LocalDateTime startDateTime) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = startDateTime.toLocalDate();

        long daysInAdvance = Duration.between(
                now.atStartOfDay(),
                startDate.atStartOfDay()
        ).toDays();

        if (daysInAdvance < 7) {
            throw new CustomException(
                    "Booking must be made at least 7 days in advance (BR-22)",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (daysInAdvance > 14) {
            throw new CustomException(
                    "Booking cannot be made more than 14 days in advance (BR-22)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-22 validated: Booking made {} days in advance", daysInAdvance);
    }

    /**
     * BR-23: Bắt buộc cọc - Nếu chưa có invoice loại DEPOSIT được thanh toán → không pickup
     */
    public void validateDepositPaidBeforePickup(Booking booking) {
        validateDepositPaid(booking); // Same as BR-06
    }

    /**
     * BR-24: Kiểm tra pin tối thiểu - Trước khi giao xe, pin phải ≥ 60%
     */
    public void validateBatteryLevel(Vehicle vehicle) {
        if (vehicle.getBatteryLevel() == null || vehicle.getBatteryLevel() < 60.0) {
            log.error("BR-24 Violation: Vehicle {} battery level is {}%",
                    vehicle.getVehicleId(), vehicle.getBatteryLevel());
            throw new CustomException(
                    "Vehicle battery level must be at least 60% before pickup (BR-24)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-24 validated: Vehicle {} battery level is {}%",
                vehicle.getVehicleId(), vehicle.getBatteryLevel());
    }

    /**
     * BR-14: Trả trễ - Tính phí theo quy định
     * - Trễ ≤ 4 giờ: tính phí theo giờ
     * - Trễ > 4 giờ: coi như trễ nguyên ngày → tính phí 1 ngày
     */
    public Double calculateLateFee(Booking booking) {
        if (booking.getActualReturnTime() == null || booking.getEndDateTime() == null) {
            return 0.0;
        }

        // Nếu trả đúng giờ hoặc sớm
        if (!booking.getActualReturnTime().isAfter(booking.getEndDateTime())) {
            return 0.0;
        }

        Duration lateDuration = Duration.between(
                booking.getEndDateTime(),
                booking.getActualReturnTime()
        );
        long lateHours = lateDuration.toHours();

        Double lateFee;
        if (lateHours <= 4) {
            // Tính theo giờ
            lateFee = lateHours * booking.getPriceSnapshotPerHour();
            log.info("BR-14: Late {} hours, charging hourly rate: {} VND",
                    lateHours, lateFee);
        } else {
            // Tính theo ngày
            long lateDays = (lateHours + 23) / 24; // Round up
            lateFee = lateDays * booking.getPriceSnapshotPerDay();
            log.info("BR-14: Late {} hours ({} days), charging daily rate: {} VND",
                    lateHours, lateDays, lateFee);
        }

        return lateFee;
    }

    /**
     * BR-15: Hoàn tất thanh toán - Người thuê phải thanh toán đầy đủ trước khi hoàn tất
     */
    public void validateFullPayment(Booking booking) {
        Invoice finalInvoice = booking.getInvoices().stream()
                .filter(inv -> inv.getType() == Invoice.Type.FINAL)
                .findFirst()
                .orElse(null);

        if (finalInvoice == null) {
            throw new CustomException(
                    "Final invoice must be created before completing booking (BR-15)",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (finalInvoice.getStatus() != Invoice.Status.PAID) {
            log.error("BR-15 Violation: Booking {} final invoice not fully paid",
                    booking.getBookingId());
            throw new CustomException(
                    "Final invoice must be fully paid before completing booking (BR-15)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-15 validated: Full payment completed for booking {}",
                booking.getBookingId());
    }

    /**
     * BR-11: Đúng trạm - Xe phải được trả tại trạm quy định
     */
    public void validateReturnStation(Booking booking, Long returnStationId) {
        Long originalStationId = booking.getVehicle().getStation().getStationId();

        if (!originalStationId.equals(returnStationId)) {
            log.error("BR-11 Violation: Vehicle must be returned to station {}, but attempting to return to station {}",
                    originalStationId, returnStationId);
            throw new CustomException(
                    "Vehicle must be returned to the original pickup station (BR-11)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-11 validated: Vehicle returned to correct station {}", originalStationId);
    }

    /**
     * BR-08: Xác thực danh tính - Người thuê phải xuất trình CCCD + GPLX
     */
    public void validateIdentityDocuments(Renter renter) {
        boolean hasCCCD = renter.getIdentityDocuments().stream()
                .anyMatch(doc -> doc.getType() == IdentityDocument.DocumentType.NATIONAL_ID
                        && doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED);

        boolean hasGPLX = renter.getIdentityDocuments().stream()
                .anyMatch(doc -> doc.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE
                        && doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED);

        if (!hasCCCD || !hasGPLX) {
            log.error("BR-08 Violation: Renter {} missing verified identity documents",
                    renter.getRenterId());
            throw new CustomException(
                    "Both CCCD and Driver License must be verified before pickup (BR-08)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("BR-08 validated: Renter {} has verified identity documents",
                renter.getRenterId());
    }
}
