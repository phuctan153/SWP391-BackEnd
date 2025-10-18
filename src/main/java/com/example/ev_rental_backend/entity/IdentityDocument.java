package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "identity_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    // 🔗 Mỗi renter có thể có nhiều giấy tờ (CCCD, GPLX,...)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    // 🪪 Loại giấy tờ: CCCD, GPLX, Hộ chiếu,...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    // 📄 Số giấy tờ (CCCD/GPLX/Passport number)
    @Column(nullable = false, length = 30)
    private String documentNumber;

    // 👤 Họ tên OCR được từ giấy tờ
    @Column(nullable = false, length = 100)
    private String fullName;

    // 📅 Ngày cấp & ngày hết hạn
    private LocalDate issueDate;
    private LocalDate expiryDate;

    // ⚙️ Trạng thái xác minh giấy tờ
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    // 🕒 Thời gian được xác minh (bởi staff hoặc OCR backend)
    private LocalDateTime verifiedAt;

    // 🕓 Thời gian tạo & cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ================== ENUMS ==================
    public enum DocumentType {
        NATIONAL_ID,      // Căn cước công dân
        DRIVER_LICENSE,   // Giấy phép lái xe
    }

    public enum DocumentStatus {
        PENDING,          // Chờ xác minh
        VERIFIED,         // Đã xác minh thành công
        REJECTED          // Từ chối (OCR lỗi hoặc giấy tờ không hợp lệ)
    }

    // ================== LIFECYCLE ==================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null)
            this.status = DocumentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
