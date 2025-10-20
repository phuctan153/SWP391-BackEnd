package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "price_list")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType; // SPARE_PART, DEPOSIT, OTHER

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    // ✅ Quan hệ 1–1 với InvoiceDetail
    @OneToOne(mappedBy = "priceList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InvoiceDetail invoiceDetail;

    public enum PriceType {
        SPARE_PART,
        DEPOSIT,
        OTHER
    }
}

