package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
        SELECT DISTINCT b
        FROM Booking b
        JOIN b.images img
        WHERE img.imageType = 'DAMAGE'
    """)
    List<Booking> findAllWithDamageReports();


    @Query("""
        SELECT b FROM Booking b
        LEFT JOIN FETCH b.renter
        LEFT JOIN FETCH b.vehicle
        LEFT JOIN FETCH b.images
        WHERE b.bookingId = :bookingId
    """)
    Optional<Booking> findBookingWithDetails(Long bookingId);

    List<Booking> findByStaff_StaffIdAndStatus(Long staffId, Booking.Status status);
}
