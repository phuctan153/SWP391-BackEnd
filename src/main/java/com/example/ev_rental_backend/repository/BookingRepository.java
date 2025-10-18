package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN b.images img
        WHERE img.imageType = 'DAMAGE'
    """)
    List<Booking> findAllWithDamageReports();
}
