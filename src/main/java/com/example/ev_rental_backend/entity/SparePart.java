package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spare_part")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SparePart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sparepartId;

    private String partName;
    private String description;
    private Double unitPrice;
    private int stockQuantity;

    @OneToOne(mappedBy = "sparePart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InvoiceDetail invoiceDetail;
}

