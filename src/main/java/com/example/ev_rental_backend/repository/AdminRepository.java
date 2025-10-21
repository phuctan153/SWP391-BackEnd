package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findFirstByStatus(Admin.Status status);
}
