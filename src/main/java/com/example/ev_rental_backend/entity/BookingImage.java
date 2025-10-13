package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    // 🔗 Nhiều ảnh thuộc về một Booking
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // 🌐 Đường dẫn ảnh
    @Column(nullable = false)
    private String imageUrl;

    // 📝 Mô tả ảnh
    private String description;

    // 🕒 Thời gian tạo ảnh
    private LocalDateTime createdAt;

    // 🖼️ Loại ảnh (trước, sau, hóa đơn, chữ ký, ...)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ImageType imageType;

    // ⏰ Tự động set thời gian tạo
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 🧩 ENUMS
    public enum ImageType {
        BEFORE_RENTAL,   // Ảnh xe trước khi thuê
        AFTER_RENTAL,    // Ảnh xe sau khi trả
        DAMAGE,          // Ảnh hư hỏng
        OTHER            // Khác
    }
}
