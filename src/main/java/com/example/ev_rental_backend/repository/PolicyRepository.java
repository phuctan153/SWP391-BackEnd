package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

        @Query("SELECT p.refundPercentRenter FROM Policy p ORDER BY p.policyId ASC LIMIT 1")
        Double getRefundPercentForRenter();

        @Query("SELECT p.refundPercentAdmin FROM Policy p ORDER BY p.policyId ASC LIMIT 1")
        Double getRefundPercentForAdmin();

        @Query("SELECT p FROM Policy p ORDER BY p.policyId ASC LIMIT 1")
        Policy getActivePolicy(); // lấy policy duy nhất của doanh nghiep
}
