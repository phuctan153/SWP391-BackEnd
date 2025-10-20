package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.BookingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingImageRepository extends JpaRepository<BookingImage, Long> {

    List<BookingImage> findByBooking_BookingId(Long bookingId);

    List<BookingImage> findByImageType(BookingImage.ImageType imageType);

    @Query("SELECT bi FROM BookingImage bi WHERE bi.booking.bookingId = :bookingId " +
            "AND bi.imageType = :imageType")
    List<BookingImage> findByBookingIdAndImageType(
            @Param("bookingId") Long bookingId,
            @Param("imageType") BookingImage.ImageType imageType
    );
}
