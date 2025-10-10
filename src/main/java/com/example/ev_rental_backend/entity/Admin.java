package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long globalAdminId;

    private String fullName;
    private String email;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { ACTIVE, INACTIVE }
}

