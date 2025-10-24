package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceDetailId;

    // 🔗 Nhiều dòng thuộc về 1 hóa đơn
    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // ⚙️ Loại dòng hóa đơn
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private LineType type;

    // 🔗 1–1 với SparePart (một dòng chỉ chứa một phụ tùng)
    @OneToOne
    @JoinColumn(name = "price_list_id", unique = true)
    private PriceList priceList;

    // 📝 Mô tả chi tiết
    private String description;

    // 🔢 Số lượng
    private int quantity;

    // 💰 Đơn giá và tổng tiền dòng
    private Double unitPrice;
    private Double lineTotal;

    // 🧩 ENUM loại dòng hóa đơn
    public enum LineType {
        SPAREPART, SERVICE, PENALTY
    }

    // 🔁 Tự động tính tổng dòng (nếu có)
    @PrePersist
    @PreUpdate
    private void calculateLineTotal() {
        if (this.quantity > 0 && this.unitPrice != null) {
            this.lineTotal = this.quantity * this.unitPrice;
        }
    }
}
