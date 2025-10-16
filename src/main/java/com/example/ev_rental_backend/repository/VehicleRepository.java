package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("""
        SELECT v FROM Vehicle v
        WHERE v.station.stationId = :stationId
        ORDER BY 
            CASE v.status
                WHEN 'MAINTENANCE' THEN 1
                WHEN 'AVAILABLE' THEN 2
                WHEN 'RESERVED' THEN 3
                WHEN 'IN_USE' THEN 4
            END
    """)
    List<Vehicle> findVehiclesByStationSorted(Long stationId);

    @Query("""
    SELECT DISTINCT v
    FROM Vehicle v
    LEFT JOIN FETCH v.bookings b
    LEFT JOIN FETCH b.renter
    LEFT JOIN FETCH b.bookingRating
    WHERE v.vehicleId = :vehicleId
    """)
    Vehicle findWithBookingsByVehicleId(@Param("vehicleId") Long vehicleId);

}
