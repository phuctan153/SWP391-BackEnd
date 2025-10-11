package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice_line")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceLineId;

    @ManyToOne @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    private LineType type;

    @ManyToOne @JoinColumn(name = "sparepart_id")
    private SparePart sparePart;

    private String description;
    private int quantity;
    private Double unitPrice;
    private Double lineTotal;

    public enum LineType { SPAREPART, SERVICE, PENALTY }
}
