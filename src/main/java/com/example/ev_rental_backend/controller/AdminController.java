package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.service.booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    private final BookingService bookingService;

    @GetMapping("/reports/damages")
    public ResponseEntity<ApiResponse<?>> getDamageReports() {
        var reports = bookingService.getBookingsWithDamages();

        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .status("success")
                        .code(200)
                        .data(reports)
                        .build()
        );
    }
}

