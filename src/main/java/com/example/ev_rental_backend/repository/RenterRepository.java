package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Repository
public interface RenterRepository extends JpaRepository<Renter, Long> {
    Optional<Renter> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // 🔹 Đăng nhập Google OAuth2
    Optional<Renter> findByGoogleId(String googleId);

    List<Renter> findByStatus(Renter.Status status);

    @Query("""
    SELECT r FROM Renter r
    JOIN r.riskProfile rp
    WHERE r.isBlacklisted = true
      AND rp.lastViolationAt < :threshold
""")
    List<Renter> findBlacklistedOver6Months(@Param("threshold") LocalDateTime threshold);
}


