package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "invoice_id")
    private String invoiceId;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private java.math.BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private java.math.BigDecimal taxAmount = java.math.BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private java.math.BigDecimal discountAmount = java.math.BigDecimal.ZERO;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private java.math.BigDecimal lateFee = java.math.BigDecimal.ZERO;

    @Column(name = "damage_fee", precision = 10, scale = 2)
    private java.math.BigDecimal damageFee = java.math.BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private java.math.BigDecimal totalAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<InvoiceLine> invoiceLines;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Payment> payments;
}

enum InvoiceStatus {
    PENDING, COMPLETED, CANCELLED, FAILED
}
