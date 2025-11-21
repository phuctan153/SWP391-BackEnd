package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    // 🔗 Mỗi Invoice thuộc về 1 Booking
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Loại hóa đơn: đặt cọc hoặc thanh toán cuối
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type = Type.FINAL;
    public enum Type { DEPOSIT, FINAL }

    // Số tiền đặt cọc (nếu có)
    @Column(nullable = false)
    private Double depositAmount = 0.0;

    // Tổng tiền cần thanh toán
    @Column(nullable = false)
    private Double totalAmount;

    // ✅ Số tiền cần HOÀN TRẢ cho khách (nếu cọc > tổng chi phí)
    @Column
    private Double refundAmount = 0.0;

    // Trạng thái thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.UNPAID;
    public enum Status { UNPAID, PARTIALLY_PAID, PAID, CANCELLED }

    // Phương thức thanh toán chính
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.CASH;
    public enum PaymentMethod { CASH, WALLET, MOMO, PAYOS }

    // Ghi chú thêm
    private String notes;

    // Ngày tạo và hoàn thành
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    // Chi tiết hóa đơn (nếu có)
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InvoiceDetail> lines = new ArrayList<>();

    // Tự động set ngày tạo khi lưu vào DB
    // Danh sách giao dịch liên quan đến hóa đơn
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceDetail> invoiceDetails;


    // Tự động set ngày tạo khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.UNPAID;
        if (this.paymentMethod == null) this.paymentMethod = PaymentMethod.CASH;
        if (this.type == null) this.type = Type.FINAL;
        if (this.depositAmount == null) this.depositAmount = 0.0;
        // 🔥 FIX: Đảm bảo lists không null
        if (this.lines == null) this.lines = new ArrayList<>();
        if (this.transactions == null) this.transactions = new ArrayList<>();
    }
}

