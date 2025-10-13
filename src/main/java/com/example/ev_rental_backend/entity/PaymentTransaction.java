package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    private LocalDateTime transactionTime;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { SUCCESS, FAILED, REVERSED }
}

