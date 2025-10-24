package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);

    // 🔹 Lấy tất cả nhân viên đang ACTIVE của 1 trạm
    @Query("""
        SELECT s FROM Staff s
        JOIN s.staffStations ss
        WHERE ss.station.stationId = :stationId
          AND ss.status = 'ACTIVE'
          AND s.status = 'ACTIVE'
    """)
    List<Staff> findActiveStaffByStation(Long stationId);

    // 🔹 Lấy danh sách nhân viên kèm tổng số booking hoàn thành (sort giảm dần)
    @Query("""
        SELECT s, COUNT(b) as completedCount
        FROM Staff s
        LEFT JOIN Booking b ON b.staff.staffId = s.staffId AND b.status = 'COMPLETED'
        JOIN s.staffStations ss
        WHERE ss.station.stationId = :stationId
        GROUP BY s
        ORDER BY completedCount DESC
    """)
    List<Object[]> findStaffWithCompletedBookingCount(Long stationId);
}
