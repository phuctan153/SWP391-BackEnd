package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 🔹 Lấy tất cả xe của 1 trạm (kể cả đang thuê / đang bảo trì)
    @Query("SELECT v FROM Vehicle v WHERE v.station.stationId = :stationId")
    List<Vehicle> findByStationId(Long stationId);

    // ✅ BR-07, BR-22: Only show vehicles that are AVAILABLE and not booked in the given range
    @Query("""
        SELECT v FROM Vehicle v
        WHERE v.status = 'AVAILABLE'
          AND v.vehicleId NOT IN (
            SELECT b.vehicle.vehicleId FROM Booking b
            WHERE (b.startDateTime <= :end AND b.endDateTime >= :start)
          )
        """)
    List<Vehicle> findAvailableVehicles(LocalDateTime start, LocalDateTime end);
}
