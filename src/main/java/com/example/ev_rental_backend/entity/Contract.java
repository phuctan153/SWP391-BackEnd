package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    // 🔗 FK → Booking
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // 🗓️ Ngày tạo/ký hợp đồng
    private LocalDateTime contractDate;

    // 📄 Loại hợp đồng (ELECTRONIC / PAPER)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ContractType contractType;

    // 📎 File hợp đồng PDF hoặc URL cloud
    @Column(length = 255)
    private String contractFileUrl;

    // ✍️ Chữ ký điện tử của người thuê
    @Column(length = 256)
    private String renterSignature;

    // ⚙️ Trạng thái hợp đồng
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private Status status;

    // 🕒 Thời gian tạo và cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ⏰ Tự động cập nhật thời gian
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING_SIGNATURE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 🧩 ENUMS
    public enum ContractType {
        ELECTRONIC, PAPER
    }

    public enum Status {
        PENDING_SIGNATURE, SIGNED, CANCELLED
    }
}
