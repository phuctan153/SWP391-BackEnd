package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(
            Notification.RecipientType recipientType, Long recipientId);
}
