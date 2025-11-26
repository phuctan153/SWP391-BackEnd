package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.BookingRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRatingRepository extends JpaRepository<BookingRating, Long> {

    BookingRating findByBooking_BookingId(Long bookingId);

    // 🔹 Trung bình rating của xe (giữ nguyên)
    @Query("""
        SELECT AVG(br.vehicleRating)
        FROM BookingRating br
        WHERE br.booking.vehicle.vehicleId = :vehicleId
    """)
    Double getAverageVehicleRating(@Param("vehicleId") Long vehicleId);

    // ✅ Trung bình rating của staff (đã cập nhật field mới)
    @Query("""
        SELECT AVG(br.staffRating)
        FROM BookingRating br
        WHERE br.booking.staffReceive.staffId = :staffId
           OR br.booking.staffReturn.staffId = :staffId
    """)
    Double getAverageStaffRating(@Param("staffId") Long staffId);
}
