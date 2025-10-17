package com.example.ev_rental_backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private Renter renter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;

    @Column(nullable = false, length = 30)
    private String documentNumber;

    private LocalDate issueDate;
    private LocalDate expiryDate;

    @Column(length = 255)
    private String frontImageUrl;

    @Column(length = 255)
    private String backImageUrl;

    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum DocumentType {
        NATIONAL_ID,
        DRIVER_LICENSE,
        PASSPORT
    }

    public enum DocumentStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }

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

