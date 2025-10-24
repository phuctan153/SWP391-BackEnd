package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;
import com.example.ev_rental_backend.dto.warning.WarningRequestDTO;
import com.example.ev_rental_backend.dto.warning.WarningResponseDTO;
import com.example.ev_rental_backend.service.booking.BookingService;
import com.example.ev_rental_backend.service.report.AdminReportService;
import com.example.ev_rental_backend.service.warning.WarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {

    private final BookingService bookingService;

    private final AdminReportService adminReportService;

    private final WarningService warningService;


    //lấy hết các booking đang có booking image có type là DAMAGED
    @GetMapping("/reports")
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

    //khi nhấn vào booking đó trên list report, sẽ hiện ra booking detail
    @GetMapping("/reports/{bookingId}")
    public ResponseEntity<ApiResponse<?>> getBookingReportDetail(@PathVariable Long bookingId) {
        try {
            BookingResponseBlacklistDTO dto = adminReportService.getBookingDetailForReport(bookingId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .code(200)
                            .data(dto)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.builder()
                            .status("error")
                            .code(500)
                            .data("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/warning")
    public ResponseEntity<ApiResponse<?>> sendWarning(@RequestBody WarningRequestDTO dto) {
        try {
            WarningResponseDTO response = warningService.sendWarningEmail(dto);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .code(200)
                            .data(response)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build()
            );
        }
    }


}

