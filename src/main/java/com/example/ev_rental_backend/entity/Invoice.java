package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoice")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @ManyToOne @JoinColumn(name = "booking_id")
    private Booking booking;

    private Type type;
    public enum Type { Deposit, Final }

    private Double depositAmount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private Status status;
    public enum Status { PENDING, COMPLETED, CANCELLED, FAILED }

    private String notes;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<InvoiceDetail> lines;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> transactions;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}

