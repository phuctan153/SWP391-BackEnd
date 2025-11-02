package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.StaffStation;
import com.example.ev_rental_backend.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffStationRepository extends JpaRepository<StaffStation, Long> {

    // 🔹 Đếm số lượng nhân viên ACTIVE tại 1 trạm cụ thể
    int countByStation_StationIdAndStatus(Long stationId, StaffStation.Status status);

    Optional<StaffStation> findTopByStaff_StaffIdAndStatusOrderByAssignedAtDesc(
            Long staffId, StaffStation.Status status);

    Optional<StaffStation> findFirstByStaff_StaffIdAndStatusOrderByAssignedAtDesc(
            Long staffId, StaffStation.Status status
    );

    List<StaffStation> findByStation_StationIdAndRoleAtStationAndStatus(
            Long stationId,
            StaffStation.RoleAtStation roleAtStation,
            StaffStation.Status status
    );

    List<StaffStation> findByStationAndStatus(Station station, StaffStation.Status status);
}
