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

    // ✅ Thuộc tính mới: Rental đã xác nhận ảnh hay chưa
    @Column(nullable = false)
    private Boolean confirmed = false;

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
//    public enum VehicleComponent {
//        EXTERIOR_FRONT,      // Ngoại thất phía trước
//        EXTERIOR_BACK,       // Ngoại thất phía sau
//        EXTERIOR_LEFT,       // Ngoại thất bên trái
//        EXTERIOR_RIGHT,      // Ngoại thất bên phải
//        STEERING_WHEEL,      // Vô lăng
//        DASHBOARD,           // Bảng điều khiển
//        SEATS,               // Ghế ngồi
//        TIRE_FRONT_LEFT,     // Bánh trước trái
//        TIRE_FRONT_RIGHT,    // Bánh trước phải
//        TIRE_REAR_LEFT,      // Bánh sau trái
//        TIRE_REAR_RIGHT,     // Bánh sau phải
//        HEADLIGHT_LEFT,      // Đèn pha trái
//        HEADLIGHT_RIGHT,     // Đèn pha phải
//        TAILLIGHT_LEFT,      // Đèn hậu trái
//        TAILLIGHT_RIGHT,     // Đèn hậu phải
//        MIRROR_LEFT,         // Gương chiếu hậu trái
//        MIRROR_RIGHT,        // Gương chiếu hậu phải
//        WINDSHIELD,          // Kính chắn gió
//        REAR_WINDOW,         // Kính sau
//        TRUNK,               // Cốp xe
//        HOOD,                // Nắp capô
//        CHARGING_PORT,       // Cổng sạc
//        INTERIOR_GENERAL,    // Nội thất tổng quan
//        MILEAGE_METER,       // Đồng hồ km
//        BATTERY_INDICATOR,   // Chỉ số pin
//        OTHER                // Khác
//    }
    public enum VehicleComponent {
        // 🔹 Hệ thống chiếu sáng
        DEN_PHA_TRUOC,
        DEN_HAU,
        DEN_XI_NHAN,
        DEN_PHANH,
        DEN_NOI_THAT,
        DEN_BIEN_SO,

        // 🔹 Kính & gương
        KINH_CHAN_GIO,
        CUA_SO_TRUOC,
        CUA_SO_SAU,
        KINH_HAU,
        GUONG_CHIEU_HAU_TRAI,
        GUONG_CHIEU_HAU_PHAI,

        // 🔹 Bánh xe & hệ thống treo
        LOP_TRUOC_TRAI,
        LOP_TRUOC_PHAI,
        LOP_SAU_TRAI,
        LOP_SAU_PHAI,
        LAZANG,
        PHUOC_GIAM_XOC,

        // 🔹 Thân xe
        NAP_CAPO,
        COP_SAU,
        CUA_TRAI,
        CUA_PHAI,
        THAN_XE,
        TEM_XE,

        // 🔹 Nội thất
        GHE_TAI_XE,
        GHE_HANH_KHACH,
        TAPLO,
        VO_LANG,
        MAN_HINH_TRUNG_TAM,
        DIEU_HOA,
        CUA_GIO,
        THAM_SAN,
        TRẦN_XE,

        // 🔹 Hệ thống điều khiển & an toàn
        PHANH_TRUOC,
        PHANH_SAU,
        DAY_AN_TOAN,
        TUI_KHI,

        // 🔹 Hệ thống điện & năng lượng
        PIN_XE,
        CONG_SAC,
        DONG_HO_PIN,
        DONG_HO_KM,
        AC_QUY,

        // 🔹 Khác
        BIEN_SO,
        KHUNG_GAM,
        ONG_XA,
        KHAC
    }
}
