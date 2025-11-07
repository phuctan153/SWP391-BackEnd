package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // 🔍 Tìm hợp đồng theo booking ID (nếu cần)
    Optional<Contract> findByBooking_BookingId(Long bookingId);

    // 🔍 Tìm hợp đồng theo trạng thái
    List<Contract> findByStatusOrderByContractDateDesc(Contract.Status status);

    Integer countByAdmin_GlobalAdminId(Long adminId);
}
