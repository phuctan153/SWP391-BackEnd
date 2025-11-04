package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.PolicyRepository;
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
    private final PolicyRepository policyRepository;

    /**
     * BR-05: Thời gian hợp lệ - Ngày, giờ bắt đầu và thời lượng thuê phải hợp lệ
     */
    public void validateBookingTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startDateTime.isBefore(now)) {
            throw new CustomException("⏰ Thời gian bắt đầu phải nằm trong tương lai (BR-05)", HttpStatus.BAD_REQUEST);
        }

        if (endDateTime.isBefore(startDateTime) || endDateTime.isEqual(startDateTime)) {
            throw new CustomException("⏳ Thời gian kết thúc phải sau thời gian bắt đầu (BR-05)", HttpStatus.BAD_REQUEST);
        }

        Duration duration = Duration.between(startDateTime, endDateTime);
        long days = duration.toDays();

        if (days < 1) {
            throw new CustomException("⛔ Thời gian thuê tối thiểu phải là 1 ngày (BR-05)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-05 hợp lệ: Thời lượng thuê {} ngày", days);
    }

    /**
     * BR-06: Đặt xe kèm cọc
     */
    public void validateDepositPaid(Booking booking) {
        boolean hasDepositPaid = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.DEPOSIT && inv.getStatus() == Invoice.Status.PAID);

        if (!hasDepositPaid) {
            throw new CustomException("💰 Cần thanh toán tiền cọc trước khi nhận xe (BR-06)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-06 hợp lệ: Đã thanh toán tiền cọc cho booking {}", booking.getBookingId());
    }

    /**
     * BR-07: Ưu tiên công bằng - Một xe không thể được nhiều khách giữ chỗ cùng lúc
     */
    public void validateVehicleAvailable(Vehicle vehicle, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (vehicle.getStatus() != Vehicle.Status.AVAILABLE) {
            throw new CustomException("Xe hiện không khả dụng (BR-07)", HttpStatus.BAD_REQUEST);
        }

        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookings(vehicle.getVehicleId(), startDateTime, endDateTime);

        if (!overlappingBookings.isEmpty()) {
            throw new CustomException("Xe đã được đặt trong khoảng thời gian này (BR-07)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-07 hợp lệ: Xe {} sẵn sàng cho thuê", vehicle.getVehicleId());
    }

    /**
     * BR-16: Giới hạn thuê 1 xe - Mỗi tài khoản chỉ được có 1 booking RESERVED hoặc IN_USE
     */
    public void validateRenterHasNoActiveBooking(Renter renter) {
        Booking hasActiveBooking = bookingRepository.findByRenter_RenterIdAndStatusIn(
                renter.getRenterId(),
                List.of(Booking.Status.RESERVED, Booking.Status.IN_USE, Booking.Status.PENDING)
        );

        if (hasActiveBooking != null) {
            throw new CustomException(
                    "🚫 Bạn đã có một đơn thuê xe đang hoạt động (BR-16). Hãy hoàn tất đơn hiện tại trước khi tạo mới. Mã đơn: "
                            + hasActiveBooking.getBookingId(),
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("✅ BR-16 hợp lệ: Renter {} không có đơn đặt xe đang hoạt động", renter.getRenterId());
    }

    /**
     * BR-22: Giới hạn đặt trước - lấy từ bảng Policy
     */
    public void validateAdvanceBookingTime(LocalDateTime startDateTime) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = startDateTime.toLocalDate();
        long daysInAdvance = Duration.between(now.atStartOfDay(), startDate.atStartOfDay()).toDays();

        double minDays = policyRepository
                .findFirstByPolicyTypeAndStatusOrderByCreatedAtDesc(
                        Policy.PolicyType.MIN_DAYS_BEFORE_BOOKING, Policy.Status.ACTIVE)
                .map(Policy::getValue)
                .orElse(7.0);

        double maxDays = policyRepository
                .findFirstByPolicyTypeAndStatusOrderByCreatedAtDesc(
                        Policy.PolicyType.MAX_DAYS_BEFORE_BOOKING, Policy.Status.ACTIVE)
                .map(Policy::getValue)
                .orElse(14.0);

        if (daysInAdvance < minDays) {
            throw new CustomException(String.format("📅 Bạn phải đặt xe trước ít nhất %.0f ngày (BR-22)", minDays), HttpStatus.BAD_REQUEST);
        }

        if (daysInAdvance > maxDays) {
            throw new CustomException(String.format("📅 Bạn không thể đặt xe trước quá %.0f ngày (BR-22)", maxDays), HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-22 hợp lệ: Đặt xe trước {} ngày (giới hạn min={}, max={})", daysInAdvance, minDays, maxDays);
    }

    /**
     * BR-23: Cọc bắt buộc trước khi nhận xe
     */
    public void validateDepositPaidBeforePickup(Booking booking) {
        validateDepositPaid(booking);
    }

    /**
     * BR-24: Kiểm tra pin tối thiểu
     */
    public void validateBatteryLevel(Vehicle vehicle) {
        if (vehicle.getBatteryLevel() == null || vehicle.getBatteryLevel() < 60.0) {
            throw new CustomException(
                    "🔋 Mức pin của xe phải đạt ít nhất 60% trước khi giao xe (BR-24)",
                    HttpStatus.BAD_REQUEST
            );
        }

        log.info("✅ BR-24 hợp lệ: Mức pin xe {} là {}%", vehicle.getVehicleId(), vehicle.getBatteryLevel());
    }

    /**
     * BR-14: Tính phí trả trễ
     */
    public Double calculateLateFee(Booking booking) {
        if (booking.getActualReturnTime() == null || booking.getEndDateTime() == null) return 0.0;
        if (!booking.getActualReturnTime().isAfter(booking.getEndDateTime())) return 0.0;

        Duration lateDuration = Duration.between(booking.getEndDateTime(), booking.getActualReturnTime());
        long lateHours = lateDuration.toHours();

        Double lateFee;
        if (lateHours <= 4) {
            lateFee = lateHours * booking.getPriceSnapshotPerHour();
            log.info("🕐 BR-14: Trễ {} giờ → tính theo giờ: {} VND", lateHours, lateFee);
        } else {
            long lateDays = (lateHours + 23) / 24;
            lateFee = lateDays * booking.getPriceSnapshotPerDay();
            log.info("🕐 BR-14: Trễ {} giờ (~{} ngày) → tính theo ngày: {} VND", lateHours, lateDays, lateFee);
        }

        return lateFee;
    }

    /**
     * BR-15: Thanh toán đầy đủ trước khi hoàn tất
     */
    public void validateFullPayment(Booking booking) {
        Invoice finalInvoice = booking.getInvoices().stream()
                .filter(inv -> inv.getType() == Invoice.Type.FINAL)
                .findFirst()
                .orElse(null);

        if (finalInvoice == null) {
            throw new CustomException("💳 Hệ thống chưa tạo hóa đơn cuối cùng cho đơn thuê (BR-15)", HttpStatus.BAD_REQUEST);
        }

        if (finalInvoice.getStatus() != Invoice.Status.PAID) {
            throw new CustomException("💳 Bạn cần thanh toán đầy đủ hóa đơn cuối cùng trước khi hoàn tất đơn thuê (BR-15)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-15 hợp lệ: Đơn {} đã thanh toán đầy đủ", booking.getBookingId());
    }

    /**
     * BR-11: Xe phải được trả đúng trạm
     */
    public void validateReturnStation(Booking booking, Long returnStationId) {
        Long originalStationId = booking.getVehicle().getStation().getStationId();

        if (!originalStationId.equals(returnStationId)) {
            throw new CustomException("📍 Xe phải được trả tại trạm ban đầu (BR-11)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-11 hợp lệ: Xe được trả đúng trạm {}", originalStationId);
    }

    /**
     * BR-08: Kiểm tra CCCD + GPLX
     */
    public void validateIdentityDocuments(Renter renter) {
        boolean hasCCCD = renter.getIdentityDocuments().stream()
                .anyMatch(doc -> doc.getType() == IdentityDocument.DocumentType.NATIONAL_ID
                        && doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED);

        boolean hasGPLX = renter.getIdentityDocuments().stream()
                .anyMatch(doc -> doc.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE
                        && doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED);

        if (!hasCCCD || !hasGPLX) {
            throw new CustomException("🪪 Bạn cần xác thực cả CCCD và Giấy phép lái xe trước khi nhận xe (BR-08)", HttpStatus.BAD_REQUEST);
        }

        log.info("✅ BR-08 hợp lệ: Renter {} đã xác thực đủ CCCD + GPLX", renter.getRenterId());
    }
}
