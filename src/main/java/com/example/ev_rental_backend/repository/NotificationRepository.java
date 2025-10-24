package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Tìm notification theo recipient
     */
    List<Notification> findByRecipientTypeAndRecipientId(
            Notification.RecipientType recipientType,
            Long recipientId
    );

    /**
     * Tìm notification chưa đọc
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = :recipientType " +
            "AND n.recipientId = :recipientId AND n.isRead = false " +
            "ORDER BY n.notificationId DESC")
    List<Notification> findUnreadNotifications(
            @Param("recipientType") Notification.RecipientType recipientType,
            @Param("recipientId") Long recipientId
    );

    /**
     * Đếm số notification chưa đọc
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientType = :recipientType " +
            "AND n.recipientId = :recipientId AND n.isRead = false")
    Long countUnreadNotifications(
            @Param("recipientType") Notification.RecipientType recipientType,
            @Param("recipientId") Long recipientId
    );
    List<Notification> findByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(
            Notification.RecipientType recipientType, Long recipientId);
}
