package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByBooking_BookingId(Long bookingId);

    @Query("SELECT i FROM Invoice i WHERE i.booking.renter.renterId = :renterId")
    List<Invoice> findByRenterId(@Param("renterId") Long renterId);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status")
    List<Invoice> findByStatus(@Param("status") Invoice.Status status);

    @Query("SELECT i FROM Invoice i WHERE i.booking.bookingId = :bookingId AND i.type = :type")
    Optional<Invoice> findByBookingIdAndType(@Param("bookingId") Long bookingId,
                                             @Param("type") Invoice.Type type);
}
