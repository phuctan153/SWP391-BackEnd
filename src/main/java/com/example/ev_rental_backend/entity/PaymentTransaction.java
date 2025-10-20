package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    // Liên kết đến Invoice (nếu giao dịch thuộc hóa đơn)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // Liên kết đến Wallet (nếu là nạp/rút hoặc thanh toán bằng ví)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    // Thời gian giao dịch
    private LocalDateTime transactionTime;

    // Số tiền giao dịch
    @Column(nullable = false)
    private BigDecimal amount;

    // Trạng thái giao dịch
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    // Loại giao dịch
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @PrePersist
    public void prePersist() {
        if (transactionTime == null) {
            transactionTime = LocalDateTime.now();
        }
    }

    // ================= Enums ================= //

    public enum Status {
        PENDING, SUCCESS, FAILED
    }

    public enum TransactionType {
        INVOICE_CASH,       // Thanh toán hóa đơn bằng tiền mặt
        INVOICE_WALLET,     // Thanh toán hóa đơn bằng ví
        INVOICE_MOMO,       // Thanh toán hóa đơn qua Momo
        WALLET_TOPUP,       // Nạp tiền vào ví
        WALLET_WITHDRAW     // Rút tiền khỏi ví
    }
}

