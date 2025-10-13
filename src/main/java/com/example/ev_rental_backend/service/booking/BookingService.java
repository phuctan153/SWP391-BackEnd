package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.dto.booking.BookingPriceRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingPriceResponseDTO;
import com.example.ev_rental_backend.dto.booking.BookingRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingResponseDTO;

public interface BookingService {
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO);
    BookingPriceResponseDTO calculatePrice(BookingPriceRequestDTO requestDTO);
}
