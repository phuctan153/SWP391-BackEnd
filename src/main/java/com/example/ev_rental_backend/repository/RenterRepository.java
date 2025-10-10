package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RenterRepository extends JpaRepository<Renter, Long> {
    Optional<Renter> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // 🔹 Đăng nhập Google OAuth2
    Optional<Renter> findByGoogleId(String googleId);

    // 🔹 Kiểm tra xác minh KYC
    Optional<Renter> findByNationalId(String nationalId);
    Optional<Renter> findByDriverLicense(String driverLicense);
}


