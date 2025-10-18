package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.entity.Booking;

import java.util.List;

public interface BookingService {
    public List<Booking> getBookingsWithDamages();
}
