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
    /**
     * Tìm invoice theo booking
     */
    List<Invoice> findByBooking_BookingId(Long bookingId);

    /**
     * Tìm invoice theo booking và type
     */
    Optional<Invoice> findByBooking_BookingIdAndType(Long bookingId, Invoice.Type type);

    /**
     * Tìm invoice kèm booking, line items, transactions
     */
    @Query("SELECT DISTINCT i FROM Invoice i " +
            "LEFT JOIN FETCH i.booking b " +
            "LEFT JOIN FETCH b.vehicle v " +
            "LEFT JOIN FETCH b.renter r " +
            "LEFT JOIN FETCH i.lines " +
            "LEFT JOIN FETCH i.transactions " +
            "WHERE i.invoiceId = :invoiceId")
    Optional<Invoice> findByIdWithDetails(@Param("invoiceId") Long invoiceId);

    /**
     * Kiểm tra booking đã có invoice Final chưa
     */
    boolean existsByBooking_BookingIdAndType(Long bookingId, Invoice.Type type);
}
