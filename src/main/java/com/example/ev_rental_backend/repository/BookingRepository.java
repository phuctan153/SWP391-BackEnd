package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    /**
     * Tìm booking theo trạng thái
     */
    List<Booking> findByStatus(Booking.Status status);

    List<Booking> findByVehicle_Station_StationId(Long stationId);

    /**
     * Tìm booking theo renter
     */
    List<Booking> findByRenter(Renter renter);

    Optional<Booking> findByBookingId(Long bookingId);

    // Kiểm tra xem renter có ít nhất 1 booking active không
    boolean existsByRenter_RenterIdAndStatusIn(Long renterId, List<Booking.Status> statuses);
    Booking findByRenter_RenterIdAndStatusIn(Long renterId, List<Booking.Status> statuses);

    /**
     * Tìm booking theo vehicle ID
     */
    List<Booking> findByVehicle_VehicleId(Long vehicleId);

    List<Booking> findByRenterOrderByCreatedAtDesc(Renter renter);

    /**
     * Tìm booking theo staff ID
     */
    List<Booking> findByStaff_StaffId(Long staffId);
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
    /**
     * Tìm các booking có thời gian trùng lặp với xe cụ thể
     * BR-07: Kiểm tra xe có sẵn trong khoảng thời gian không
     */
    @Query("SELECT b FROM Booking b WHERE b.vehicle.vehicleId = :vehicleId " +
            "AND b.status IN ( com.example.ev_rental_backend.entity.Booking.Status.RESERVED, com.example.ev_rental_backend.entity.Booking.Status.IN_USE) " +
            "AND ((b.startDateTime <= :endDateTime AND b.endDateTime >= :startDateTime))")
    List<Booking> findOverlappingBookings(
            @Param("vehicleId") Long vehicleId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * Tìm các booking sắp bắt đầu trong vòng N giờ (cho BR-20: nhắc nhở)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' " +
            "AND b.startDateTime BETWEEN :now AND :futureTime")
    List<Booking> findBookingsStartingSoon(
            @Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime
    );

    /**
     * Tìm các booking đã hết hạn (BR-21: tự động hủy)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' " +
            "AND b.expiresAt < :now")
    List<Booking> findExpiredBookings(@Param("now") LocalDateTime now);

    /**
     * Tìm booking theo renter ID và trạng thái
     */
    @Query("SELECT b FROM Booking b WHERE b.renter.renterId = :renterId " +
            "AND b.status = :status")
    List<Booking> findByRenterIdAndStatus(
            @Param("renterId") Long renterId,
            @Param("status") Booking.Status status
    );

    /**
     * Đếm số booking của renter
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.renter.renterId = :renterId")
    Long countByRenterId(@Param("renterId") Long renterId);

    /**
     * Đếm số booking hoàn thành của renter
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.renter.renterId = :renterId " +
            "AND b.status = 'COMPLETED'")
    Long countCompletedBookingsByRenterId(@Param("renterId") Long renterId);

    /**
     * Tìm booking theo khoảng thời gian
     */
    @Query("SELECT b FROM Booking b WHERE b.startDateTime >= :startDate " +
            "AND b.endDateTime <= :endDate")
    List<Booking> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm booking cần trả trong ngày hôm nay
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'IN_USE' " +
            "AND DATE(b.endDateTime) = DATE(:today)")
    List<Booking> findBookingsDueToday(@Param("today") LocalDateTime today);

    List<Booking> findByRenterAndStatus(Renter renter, Booking.Status bookingStatus);
}
