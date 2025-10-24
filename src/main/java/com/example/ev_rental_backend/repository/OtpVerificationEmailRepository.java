package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.OtpVerificationEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationEmailRepository extends JpaRepository<OtpVerificationEmail, Long> {

    // Lấy OTP mới nhất của renter
    @Query("""
        SELECT o FROM OtpVerificationEmail o
        WHERE o.renter.renterId = :renterId
        ORDER BY o.createdAt DESC
        LIMIT 1
    """)
    Optional<OtpVerificationEmail> findLatestOtpByRenterId(Long renterId);


    @Query("""
    SELECT COUNT(o) > 0
    FROM OtpVerificationEmail o
    WHERE o.renter.renterId = :renterId
      AND o.status = 'VERIFIED'
    """)
    boolean existsVerifiedOtpForRenter(@Param("renterId") Long renterId);

}
