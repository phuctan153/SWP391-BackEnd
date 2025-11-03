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

    // 🔧 Hạng mục xe (vô lăng, bánh xe, đèn, gương, ...)
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private VehicleComponent vehicleComponent;

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

    // 🔧 Hạng mục kiểm tra xe
    public enum VehicleComponent {
        EXTERIOR_FRONT,      // Ngoại thất phía trước
        EXTERIOR_BACK,       // Ngoại thất phía sau
        EXTERIOR_LEFT,       // Ngoại thất bên trái
        EXTERIOR_RIGHT,      // Ngoại thất bên phải
        STEERING_WHEEL,      // Vô lăng
        DASHBOARD,           // Bảng điều khiển
        SEATS,               // Ghế ngồi
        TIRE_FRONT_LEFT,     // Bánh trước trái
        TIRE_FRONT_RIGHT,    // Bánh trước phải
        TIRE_REAR_LEFT,      // Bánh sau trái
        TIRE_REAR_RIGHT,     // Bánh sau phải
        HEADLIGHT_LEFT,      // Đèn pha trái
        HEADLIGHT_RIGHT,     // Đèn pha phải
        TAILLIGHT_LEFT,      // Đèn hậu trái
        TAILLIGHT_RIGHT,     // Đèn hậu phải
        MIRROR_LEFT,         // Gương chiếu hậu trái
        MIRROR_RIGHT,        // Gương chiếu hậu phải
        WINDSHIELD,          // Kính chắn gió
        REAR_WINDOW,         // Kính sau
        TRUNK,               // Cốp xe
        HOOD,                // Nắp capô
        CHARGING_PORT,       // Cổng sạc
        INTERIOR_GENERAL,    // Nội thất tổng quan
        MILEAGE_METER,       // Đồng hồ km
        BATTERY_INDICATOR,   // Chỉ số pin
        OTHER                // Khác
    }
}
