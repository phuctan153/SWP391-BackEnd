package com.example.ev_rental_backend.service.notification;

import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
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
}
