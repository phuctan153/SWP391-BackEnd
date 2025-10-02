package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spare_parts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SparePart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sparepart_id")
    private Long sparepartId;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private java.math.BigDecimal unitPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @OneToMany(mappedBy = "sparePart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<InvoiceLine> invoiceLines;
}
