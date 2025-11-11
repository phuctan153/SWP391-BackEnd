package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "price_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    // 🔹 Loại bảng giá (phụ tùng hoặc dịch vụ khác)
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 50)
    private PriceType priceType; // SPARE_PART, OTHER

    // 🔹 Tên phụ tùng (nếu là SPARE_PART)
    @Enumerated(EnumType.STRING)
    @Column(name = "spare_part_type", length = 50)
    private SparePartType sparePartType; // ví dụ: HEADLIGHT, MIRROR, TIRE, BATTERY,...

    // 🔹 Tên hiển thị (mô tả rõ ràng hơn)
    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "description", length = 255)
    private String description;

    // 💰 Đơn giá
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    // 📦 Số lượng tồn kho
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    // ✅ Quan hệ 1–1 với InvoiceDetail
//    @OneToOne(mappedBy = "priceList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private InvoiceDetail invoiceDetail;

    // 🔹 Loại bảng giá
    public enum PriceType {
        SPARE_PART, // phụ tùng
        OTHER       // các loại chi phí khác
    }

    // 🔹 Enum phụ tùng cụ thể
    @Getter
    public enum SparePartType {
        HEADLIGHT("Đèn xe"),
        TAILLIGHT("Đèn hậu"),
        MIRROR("Gương chiếu hậu"),
        TIRE("Lốp xe"),
        BATTERY("Pin xe"),
        SCREEN("Màn hình điều khiển"),
        BRAKE("Phanh xe"),
        SEAT("Ghế ngồi"),
        HANDLE("Tay lái"),
        OTHER("Khác");

        private final String displayName;

        SparePartType(String displayName) {
            this.displayName = displayName;
        }

    }
}
