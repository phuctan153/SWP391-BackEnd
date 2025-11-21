package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "staff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;  // 🟢 thêm field password

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;

    public enum Status { ACTIVE, INACTIVE }

    // 🔹 Các booking mà nhân viên này phụ trách bàn giao xe (pickup)
    @OneToMany(mappedBy = "staffReceive", fetch = FetchType.LAZY)
    private List<Booking> receivedBookings;

    // 🔹 Các booking mà nhân viên này phụ trách nhận lại xe (return)
    @OneToMany(mappedBy = "staffReturn", fetch = FetchType.LAZY)
    private List<Booking> returnedBookings;

    @OneToMany(mappedBy = "staff")
    private List<StaffStation> staffStations;

    // ✅ Gán mặc định thời gian tạo
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = Status.ACTIVE;
        }
    }
}
