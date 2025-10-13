package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.booking.BookingPriceRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingPriceResponseDTO;
import com.example.ev_rental_backend.dto.booking.BookingRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingResponseDTO;
import com.example.ev_rental_backend.service.booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody BookingRequestDTO requestDTO) {
        BookingResponseDTO response = bookingService.createBooking(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<BookingPriceResponseDTO> calculatePrice(
            @RequestBody BookingPriceRequestDTO requestDTO) {
        BookingPriceResponseDTO response = bookingService.calculatePrice(requestDTO);
        return ResponseEntity.ok(response);
    }

}
