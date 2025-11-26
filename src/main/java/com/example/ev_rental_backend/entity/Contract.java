package com.example.ev_rental_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
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

    // 🔗 FK → Booking (1-1)
    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Builder.Default
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TermCondition> terms = new java.util.ArrayList<>();

    // 🔗 FK → Admin (n-1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    // 🗓️ Ngày tạo hợp đồng
    private LocalDateTime contractDate;

    // 📄 Loại hợp đồng (ELECTRONIC / PAPER)
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ContractType contractType;

    // 📎 File hợp đồng PDF hoặc URL cloud
    @Column(length = 255)
    private String contractFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id")
    private Staff createdByStaff;

    // ✍️ Chữ ký điện tử của admin (base64 hoặc URL ảnh)
    @Column(length = 512)
    private String adminSignature;

    // 🕓 Thời điểm admin ký hợp đồng
    private LocalDateTime adminSignedAt;

    // ✍️ Chữ ký điện tử của renter (base64 hoặc URL ảnh)
    @Column(length = 512)
    private String renterSignature;

    // 🕓 Thời điểm renter ký hợp đồng
    private LocalDateTime renterSignedAt;

    // ⚙️ Trạng thái hợp đồng
    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private Status status;

    // 🕒 Thời gian tạo và cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========================== LIFECYCLE ==========================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = Status.PENDING_ADMIN_SIGNATURE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================== ENUMS ==========================

    public enum ContractType {
        ELECTRONIC, PAPER
    }

    public enum Status {
        PENDING_ADMIN_SIGNATURE, // 🕓 Hợp đồng được tạo, chờ admin ký duyệt
        ADMIN_SIGNED,            // 🖊️ Admin đã ký, chờ renter ký
        FULLY_SIGNED,            // ✅ Cả admin và renter đều đã ký
        CANCELLED                // ❌ Hợp đồng bị hủy
    }
}
