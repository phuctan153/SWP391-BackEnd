package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> getBookingsWithDamages() {
        return bookingRepository.findAllWithDamageReports();
    }
}
