package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.StaffStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffStationRepository extends JpaRepository<StaffStation, Long> {

    // 🔹 Đếm số lượng nhân viên ACTIVE tại 1 trạm cụ thể
    int countByStation_StationIdAndStatus(Long stationId, StaffStation.Status status);
}
