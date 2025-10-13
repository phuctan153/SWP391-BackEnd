package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByRenterAndStatusIn(Renter renter, List<Booking.Status> statuses);
}
