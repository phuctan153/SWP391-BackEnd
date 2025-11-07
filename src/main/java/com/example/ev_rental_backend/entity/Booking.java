package com.example.ev_rental_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    // 🔗 Mối quan hệ
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // 💵 Giá snapshot tại thời điểm đặt xe
    @Column(nullable = false)
    private Double priceSnapshotPerHour;

    @Column(nullable = false)
    private Double priceSnapshotPerDay;

    // ⏱️ Thời gian thuê xe
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // ⏰ Khi hệ thống hủy vì không thanh toán đúng hạn
    private LocalDateTime expiresAt;

    // ⏱️ Thời gian trả xe thực tế
    private LocalDateTime actualReturnTime;

    // ⚙️ Trạng thái đơn đặt xe
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    // 💰 Tổng số tiền
    private Double totalAmount;

    // 💳 Trạng thái đặt cọc
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DepositStatus depositStatus;

    // 🕒 Thời gian tạo và cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔗 Liên kết với Contract (1–1)
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Contract contract;

    // 🔗 Hóa đơn (1–N)
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Invoice> invoices;

    // 🔗 Hình ảnh (1–N)
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingImage> images;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BookingRating bookingRating;


    // ⏰ Tự động cập nhật thời gian
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.RESERVED;
        }
        if (this.depositStatus == null) {
            this.depositStatus = DepositStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🧩 ENUMS
    public enum Status {
        PENDING, RESERVED, IN_USE, COMPLETED, CANCELLED, EXPIRED
    }

    public enum DepositStatus {
        PENDING, PAID, REFUNDED
    }
}
