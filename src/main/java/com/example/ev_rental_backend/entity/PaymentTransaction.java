package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.*;
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
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.PENDING;

    // Loại giao dịch
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    // trường orderCode
    @Column(unique = true)
    private Long orderCode;

    // 🆕 Payment Link ID từ PayOS
    private String paymentLinkId;

    // 🆕 Reference code từ PayOS (mã giao dịch thực tế)
    private String referenceCode;

    // 🆕 Ghi chú / Lý do (VD: lý do failed, cancelled)
    @Column(length = 500)
    private String notes;

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
        INVOICE_PAYOS,      // Thanh toán hóa đơn qua Payos
        WALLET_TOPUP,       // Nạp tiền vào ví
        WALLET_WITHDRAW,     // Rút tiền khỏi ví
        WALLET_REFUND_DEPOSIT,
        REFUND_CASH
    }
}
