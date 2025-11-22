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

    // 🔹 Loại bảng giá
    public enum PriceType {
        SPARE_PART, // phụ tùng
        OTHER       // các loại chi phí khác
    }

    // 🔹 Enum phụ tùng cụ thể
    @Getter
    public enum SparePartType {
//        HEADLIGHT("Đèn xe"),
//        TAILLIGHT("Đèn hậu"),
//        MIRROR("Gương chiếu hậu"),
//        TIRE("Lốp xe"),
//        BATTERY("Pin xe"),
//        SCREEN("Màn hình điều khiển"),
//        BRAKE("Phanh xe"),
//        SEAT("Ghế ngồi"),
//        HANDLE("Tay lái"),
//        OTHER("Khác");
// 🔹 Hệ thống chiếu sáng
DEN_PHA_TRUOC("Đèn pha trước"),
        DEN_HAU("Đèn hậu"),
        DEN_XI_NHAN("Đèn xi-nhan"),
        DEN_PHANH("Đèn phanh"),
        DEN_NOI_THAT("Đèn nội thất"),
        DEN_BIEN_SO("Đèn biển số"),

        // 🔹 Kính & gương
        KINH_CHAN_GIO("Kính chắn gió"),
        CUA_SO_TRUOC("Cửa sổ trước"),
        CUA_SO_SAU("Cửa sổ sau"),
        KINH_HAU("Kính hậu"),
        GUONG_CHIEU_HAU_TRAI("Gương chiếu hậu trái"),
        GUONG_CHIEU_HAU_PHAI("Gương chiếu hậu phải"),

        // 🔹 Bánh xe & hệ thống treo
        LOP_TRUOC_TRAI("Lốp trước trái"),
        LOP_TRUOC_PHAI("Lốp trước phải"),
        LOP_SAU_TRAI("Lốp sau trái"),
        LOP_SAU_PHAI("Lốp sau phải"),
        LAZANG("Mâm / lazăng"),
        PHUOC_GIAM_XOC("Phuộc giảm xóc"),

        // 🔹 Thân xe
        NAP_CAPO("Nắp capo"),
        COP_SAU("Cốp sau"),
        CUA_TRAI("Cửa trái"),
        CUA_PHAI("Cửa phải"),
        THAN_XE("Thân xe"),
        TEM_XE("Tem xe / logo"),

        // 🔹 Nội thất
        GHE_TAI_XE("Ghế tài xế"),
        GHE_HANH_KHACH("Ghế hành khách"),
        TAPLO("Taplo / bảng điều khiển"),
        VO_LANG("Vô lăng"),
        MAN_HINH_TRUNG_TAM("Màn hình trung tâm"),
        DIEU_HOA("Hệ thống điều hòa"),
        CUA_GIO("Cửa gió điều hòa"),
        THAM_SAN("Thảm sàn"),
        TRAN_XE("Trần xe"),

        // 🔹 Hệ thống điều khiển & an toàn
        PHANH_TRUOC("Phanh trước"),
        PHANH_SAU("Phanh sau"),
        DAY_AN_TOAN("Dây an toàn"),
        TUI_KHI("Túi khí"),

        // 🔹 Hệ thống điện & năng lượng
        PIN_XE("Pin xe"),
        CONG_SAC("Cổng sạc"),
        DONG_HO_PIN("Đồng hồ pin"),
        DONG_HO_KM("Đồng hồ km"),
        AC_QUY("Ắc quy"),

        // 🔹 Khác
        BIEN_SO("Biển số xe"),
        KHUNG_GAM("Khung gầm"),
        ONG_XA("Ống xả"),
        KHAC("Khác");

        private final String displayName;

        SparePartType(String displayName) {
            this.displayName = displayName;
        }

    }
}
