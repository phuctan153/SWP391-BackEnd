package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Contract;
import com.example.ev_rental_backend.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByContractOrderByCreatedAtDesc(Contract contract);
}
