package com.example.ev_rental_backend.service.notification;

import com.example.ev_rental_backend.entity.Notification;

import java.util.List;

public interface NotificationService {
    public List<Notification> getAllNotificationsForAdmin(Long adminId);
    public void markAsRead(Long notificationId);

    Notification sendNotificationToAdmin(Long adminId, String title, String message);
}
