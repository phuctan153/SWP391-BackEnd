package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.entity.Policy.PolicyType;
import com.example.ev_rental_backend.entity.Policy.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    // 🔹 Lấy policy đang active theo loại
    Optional<Policy> findFirstByPolicyTypeAndStatusOrderByCreatedAtDesc(PolicyType policyType, Status status);

    // 🔹 Lấy tất cả policy theo trạng thái
    List<Policy> findByStatus(Status status);

    // 🔹 Lấy tất cả policy theo loại
    List<Policy> findByPolicyType(PolicyType policyType);
}
