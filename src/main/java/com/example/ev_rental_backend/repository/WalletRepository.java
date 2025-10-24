package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByRenter(Renter renter);

    // 🔹 Tìm ví theo renterId (nếu cần dùng trong service)
    Optional<Wallet> findByRenter_RenterId(Long renterId);

    @Query("SELECT w FROM Wallet w WHERE w.status = :status")
    List<Wallet> findByStatus(@Param("status") Wallet.Status status);
    // 🔹 Kiểm tra renter đã có ví hay chưa
    boolean existsByRenter(Renter renter);
}
