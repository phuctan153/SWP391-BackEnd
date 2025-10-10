package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    private Method method;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Double amount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum Method { CASH, MOMO }
    public enum Status { PENDING, COMPLETED, FAILED, CANCELLED }

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<PaymentTransaction> transactions;
}

