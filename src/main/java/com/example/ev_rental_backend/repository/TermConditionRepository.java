package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Contract;
import com.example.ev_rental_backend.entity.TermCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermConditionRepository extends JpaRepository<TermCondition, String> {

    // 🔍 Lấy danh sách điều khoản theo hợp đồng
    List<TermCondition> findByContract(Contract contract);
}
