package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {

    // 🔹 Lấy tất cả trạm (kể cả trạm không có xe khả dụng)
    @Query("SELECT DISTINCT s FROM Station s LEFT JOIN FETCH s.vehicles v")
    List<Station> findAllStationsWithVehicles();

    //Tim trạm theo tên không phân biệt hoa thường
    Optional<Station> findByNameIgnoreCase(String name);

    //Kiểm tra tồn tại trạm theo tên không phân biệt hoa thường
    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT s FROM Station s WHERE " +
            "ABS(s.latitude - :latitude) < 0.001 AND " +
            "ABS(s.longitude - :longitude) < 0.001")
    List<Station> findByNearbyCoordinates(Double latitude, Double longitude);

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
