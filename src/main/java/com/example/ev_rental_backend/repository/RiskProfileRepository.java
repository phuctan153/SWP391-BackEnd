package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfile, Long> {
    Optional<RiskProfile> findByRenter(Renter renter);
}
