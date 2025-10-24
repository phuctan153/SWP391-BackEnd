package com.example.ev_rental_backend.scheduler;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.NotificationRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import com.example.ev_rental_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled Jobs cho Booking Management
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduledJobs {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * BR-20: Gửi nhắc nhở nhận xe trước 1 ngày
     * Chạy mỗi ngày lúc 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendPickupReminders() {
        log.info("Running scheduled job: Send pickup reminders");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        // Tìm các booking bắt đầu trong vòng 24-25 giờ tới
        List<Booking> upcomingBookings = bookingRepository.findBookingsStartingSoon(
                tomorrow.minusHours(1),
                tomorrow.plusHours(1)
        );

        for (Booking booking : upcomingBookings) {
            try {
                notificationService.sendPickupReminder(booking);
                log.info("Pickup reminder sent for booking {}", booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to send pickup reminder for booking {}",
                        booking.getBookingId(), e);
            }
        }

        log.info("Pickup reminders sent: {} bookings", upcomingBookings.size());
    }

    /**
     * BR-21: Tự động hủy booking nếu quá 1h không nhận xe
     * Chạy mỗi 10 phút
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void autoExpireBookings() {
        log.info("Running scheduled job: Auto-expire bookings");

        LocalDateTime now = LocalDateTime.now();

        // Tìm các booking đã hết hạn
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now);

        for (Booking booking : expiredBookings) {
            try {
                // Cập nhật trạng thái booking
                booking.setStatus(Booking.Status.EXPIRED);
                bookingRepository.save(booking);

                // Giải phóng vehicle
                Vehicle vehicle = booking.getVehicle();
                vehicle.setStatus(Vehicle.Status.AVAILABLE);
                vehicleRepository.save(vehicle);

                // Gửi thông báo
                notificationService.sendBookingExpired(booking);

                log.info("Booking {} auto-expired", booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to auto-expire booking {}", booking.getBookingId(), e);
            }
        }

        log.info("Bookings auto-expired: {}", expiredBookings.size());
    }

    /**
     * Nhắc nhở trả xe cho các booking sắp đến hạn
     * Chạy mỗi giờ
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void sendReturnReminders() {
        log.info("Running scheduled job: Send return reminders");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in2Hours = now.plusHours(2);

        // Tìm các booking IN_USE và sắp đến hạn trả trong 2 giờ tới
        List<Booking> bookingsDueSoon = bookingRepository.findByStatus(Booking.Status.IN_USE)
                .stream()
                .filter(b -> b.getEndDateTime().isAfter(now)
                        && b.getEndDateTime().isBefore(in2Hours))
                .toList();

        for (Booking booking : bookingsDueSoon) {
            try {
                Notification notification = Notification.builder()
                        .title("⏰ Nhắc nhở: Sắp đến hạn trả xe")
                        .message(String.format(
                                "Đặt xe của bạn sẽ hết hạn lúc %s. " +
                                        "Vui lòng trả xe đúng giờ để tránh phí trễ hạn.",
                                booking.getEndDateTime()
                        ))
                        .recipientType(Notification.RecipientType.RENTER)
                        .recipientId(booking.getRenter().getRenterId())
                        .isRead(false)
                        .build();

                notificationRepository.save(notification);
                log.info("Return reminder sent for booking {}", booking.getBookingId());
            } catch (Exception e) {
                log.error("Failed to send return reminder for booking {}",
                        booking.getBookingId(), e);
            }
        }

        log.info("Return reminders sent: {}", bookingsDueSoon.size());
    }

    /**
     * Cảnh báo cho staff về các xe cần bảo trì
     * Chạy mỗi ngày lúc 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkVehicleMaintenanceNeeded() {
        log.info("Running scheduled job: Check vehicle maintenance");

        List<Vehicle> vehicles = vehicleRepository.findAll();

        for (Vehicle vehicle : vehicles) {
            // Kiểm tra xe cần bảo trì (ví dụ: mileage > 10000 km)
            if (vehicle.getMileage() != null && vehicle.getMileage() > 10000) {
                if (vehicle.getStatus() != Vehicle.Status.MAINTENANCE) {
                    vehicle.setStatus(Vehicle.Status.MAINTENANCE);
                    vehicleRepository.save(vehicle);

                    log.warn("Vehicle {} needs maintenance (mileage: {} km)",
                            vehicle.getVehicleId(), vehicle.getMileage());
                }
            }
        }
    }
}
