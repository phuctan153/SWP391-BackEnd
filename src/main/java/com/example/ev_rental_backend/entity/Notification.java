package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType; // RENTER, STAFF, ADMIN

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId; // ID của người nhận

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // false = chưa đọc, true = đã đọc

    // ✅ Enum định nghĩa loại người nhận
    public enum RecipientType {
        RENTER,
        STAFF,
        ADMIN
    }

    // (Tuỳ chọn) Nếu bạn muốn thêm thời gian tạo tự động
    @PrePersist
    protected void onCreate() {
        if (isRead == null) {
            isRead = false;
        }
    }
}
