package com.example.ev_rental_backend.service.notification;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Notification;

import java.util.List;

public interface NotificationService {
    public void sendPickupReminder(Booking booking);
    public void sendBookingCancelled(Booking booking);
    public void sendBookingExpired(Booking booking);
    public void sendBookingCompleted(Booking booking);
    public void sendPaymentSuccess(Booking booking, Double amount);
    public void sendDepositRefunded(Booking booking, Double amount);
    public void sendNewBookingToStaff(Booking booking, Long staffId);
    public List<Notification> getAllNotificationsForAdmin(Long adminId);
    public void markAsRead(Long notificationId);

    Notification sendNotificationToAdmin(Long adminId, String title, String message);
}
