package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // 🔹 Lấy tất cả xe của 1 trạm (kể cả đang thuê / đang bảo trì)
    @Query("SELECT v FROM Vehicle v WHERE v.station.stationId = :stationId")
    List<Vehicle> findByStationId(Long stationId);
}
