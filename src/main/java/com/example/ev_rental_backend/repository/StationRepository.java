package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    // 🔹 Lấy tất cả trạm (kể cả trạm không có xe khả dụng)
    @Query("SELECT DISTINCT s FROM Station s LEFT JOIN FETCH s.vehicles v")
    List<Station> findAllStationsWithVehicles();

    @Query("""
    SELECT s.stationId, s.name, COUNT(b)
    FROM Station s
    JOIN s.vehicles v
    JOIN v.bookings b
    WHERE b.status IN ('RESERVED', 'IN_USE')
    GROUP BY s.stationId, s.name
    """)
    List<Object[]> countActiveBookingsByStation();
}
