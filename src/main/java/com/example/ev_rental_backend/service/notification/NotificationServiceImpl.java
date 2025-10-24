package com.example.ev_rental_backend.service.notification;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> getAllNotificationsForAdmin(Long adminId) {
        return notificationRepository.findByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(
                Notification.RecipientType.ADMIN, adminId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }

    @Override
    public Notification sendNotificationToAdmin(Long adminId, String title, String message) {
        Notification notification = Notification.builder()
                .recipientType(Notification.RecipientType.ADMIN)
                .recipientId(adminId)
                .title(title)
                .message(message)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    /**
     * Gửi thông báo nhắc nhở nhận xe (BR-20)
     */
    public void sendPickupReminder(Booking booking) {
        Notification notification = Notification.builder()
                .title("⏰ Nhắc nhở: Đặt xe sắp bắt đầu")
                .message(String.format(
                        "Đặt xe của bạn sẽ bắt đầu vào ngày mai (%s). " +
                                "Vui lòng đến trạm và hoàn tất thủ tục nhận xe đúng giờ để tránh bị hủy tự động.",
                        booking.getStartDateTime()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Pickup reminder sent to renter {}", booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo booking đã bị hủy
     */
    public void sendBookingCancelled(Booking booking) {
        Notification notification = Notification.builder()
                .title("❌ Đặt xe đã bị hủy")
                .message(String.format(
                        "Đặt xe #%d của bạn đã bị hủy.",
                        booking.getBookingId()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Cancellation notification sent to renter {}", booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo booking đã hết hạn (BR-21)
     */
    public void sendBookingExpired(Booking booking) {
        Notification notification = Notification.builder()
                .title("⚠️ Đặt xe đã hết hạn")
                .message(
                        "Đặt xe của bạn đã bị hủy vì không hoàn tất thủ tục nhận xe " +
                                "trong vòng 1 giờ kể từ thời điểm bắt đầu thuê."
                )
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Expiration notification sent to renter {}", booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo booking đã hoàn thành
     */
    public void sendBookingCompleted(Booking booking) {
        Notification notification = Notification.builder()
                .title("✅ Đặt xe hoàn thành")
                .message(String.format(
                        "Cảm ơn bạn đã sử dụng dịch vụ! Đặt xe #%d đã hoàn thành. " +
                                "Vui lòng đánh giá trải nghiệm của bạn.",
                        booking.getBookingId()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Completion notification sent to renter {}", booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo thanh toán thành công
     */
    public void sendPaymentSuccess(Booking booking, Double amount) {
        Notification notification = Notification.builder()
                .title("💳 Thanh toán thành công")
                .message(String.format(
                        "Bạn đã thanh toán thành công %.0f VND cho đặt xe #%d.",
                        amount, booking.getBookingId()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Payment success notification sent to renter {}",
                booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo hoàn tiền cọc
     */
    public void sendDepositRefunded(Booking booking, Double amount) {
        Notification notification = Notification.builder()
                .title("💰 Hoàn tiền cọc")
                .message(String.format(
                        "Tiền cọc %.0f VND đã được hoàn vào ví của bạn.",
                        amount
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(booking.getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Deposit refund notification sent to renter {}",
                booking.getRenter().getRenterId());
    }

    /**
     * Gửi thông báo cho staff về booking mới
     */
    public void sendNewBookingToStaff(Booking booking, Long staffId) {
        Notification notification = Notification.builder()
                .title("📋 Đặt xe mới")
                .message(String.format(
                        "Có đặt xe mới #%d từ khách hàng %s.",
                        booking.getBookingId(), booking.getRenter().getFullName()
                ))
                .recipientType(Notification.RecipientType.STAFF)
                .recipientId(staffId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("New booking notification sent to staff {}", staffId);
    }
}
