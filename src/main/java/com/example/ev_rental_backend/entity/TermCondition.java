package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "term_condition")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TermCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "term_condition_id", updatable = false, nullable = false)
    private String termConditionId;

    @Column(name = "term_number", nullable = false)
    private Integer termNumber;

    @Column(name = "term_title", nullable = false, length = 255)
    private String termTitle;

    @Column(name = "term_content", columnDefinition = "TEXT")
    private String termContent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
