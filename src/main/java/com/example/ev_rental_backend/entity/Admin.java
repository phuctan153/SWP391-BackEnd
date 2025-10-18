package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalAdminId;

    private String password;
    private String fullName;
    private String email;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    // 🔗 1 Admin có thể ký nhiều hợp đồng
    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Contract> contracts;

    public enum Status {
        ACTIVE, INACTIVE
    }
}
