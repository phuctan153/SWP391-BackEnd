package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.*;
import com.example.ev_rental_backend.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ==================== 5.1. Booking Creation ====================

    /**
     * POST /api/bookings - Tạo booking mới (RESERVED)
     * BR-05, BR-06, BR-07, BR-16, BR-22
     */
    @PostMapping
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Valid @RequestBody CreateBookingRequestDto requestDto) {
        BookingResponseDto booking = bookingService.createBooking(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(booking)
                        .build());
    }

    /**
     * GET /api/bookings/{bookingId} - Chi tiết booking
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> getBookingById(
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    /**
     * PUT /api/bookings/{bookingId}/cancel - Hủy booking (trước khi nhận xe)
     */
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody(required = false) CancelBookingRequestDto requestDto) {
        BookingResponseDto booking = bookingService.cancelBooking(bookingId, requestDto);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    // ==================== 5.2. Booking Images ====================

    /**
     * POST /api/bookings/{bookingId}/images - Upload ảnh xe (BR-09, BR-26)
     */
    @PostMapping(
            value = "/{bookingId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingImageResponseDto>> uploadBookingImage(
            @PathVariable Long bookingId,
            @ModelAttribute("file") MultipartFile file,
            @RequestParam("imageType") String imageType,
            @RequestParam(value = "description", required = false) String description) {

        BookingImageResponseDto image = bookingService.uploadBookingImage(
                bookingId, file, imageType, description);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingImageResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(image)
                        .build());
    }

    /**
     * GET /api/bookings/{bookingId}/images - Lấy danh sách ảnh
     */
    @GetMapping("/{bookingId}/images")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingImageResponseDto>>> getBookingImages(
            @PathVariable Long bookingId) {
        List<BookingImageResponseDto> images = bookingService.getBookingImages(bookingId);
        return ResponseEntity.ok(ApiResponse.<List<BookingImageResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(images)
                .build());
    }

    // ==================== 5.3. Pickup Process ====================

    /**
     * POST /api/bookings/{bookingId}/pickup - Check-in nhận xe (BR-08, BR-09, BR-10, BR-23, BR-24)
     */
    @PostMapping("/{bookingId}/pickup")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> pickupVehicle(
            @PathVariable Long bookingId,
            @Valid @RequestBody PickupRequestDto requestDto) {
        BookingResponseDto booking = bookingService.pickupVehicle(bookingId, requestDto);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    /**
     * PUT /api/bookings/{bookingId}/status/in-use - Chuyển trạng thái sang IN_USE
     */
    @PutMapping("/{bookingId}/status/in-use")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateStatusToInUse(
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.updateStatusToInUse(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    @PutMapping("/{bookingId}/status/reserved")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateStatusToReserved(
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.updateStatusToReserved(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    // ==================== 5.4. Return Process ====================

    /**
     * POST /api/bookings/{bookingId}/return - Trả xe (BR-11, BR-12, BR-13, BR-14, BR-15, BR-26, BR-27)
     */
    @PostMapping("/{bookingId}/return")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReturnResponseDto>> returnVehicle(
            @PathVariable Long bookingId,
            @Valid @RequestBody ReturnRequestDto requestDto) {
        ReturnResponseDto response = bookingService.returnVehicle(bookingId, requestDto);
        return ResponseEntity.ok(ApiResponse.<ReturnResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(response)
                .build());
    }

    /**
     * PUT /api/bookings/{bookingId}/status/completed - Hoàn tất booking
     */
    @PutMapping("/{bookingId}/status/completed")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponseDto>> completeBooking(
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    // ==================== 5.5. Booking Rating ====================

    /**
     * POST /api/bookings/{bookingId}/rating - Đánh giá sau khi hoàn thành
     */
    @PostMapping("/{bookingId}/rating")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<BookingRatingResponseDto>> rateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateBookingRatingDto requestDto) {
        BookingRatingResponseDto rating = bookingService.rateBooking(bookingId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingRatingResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(rating)
                        .build());
    }

    /**
     * GET /api/bookings/{bookingId}/rating - Xem đánh giá
     */
    @GetMapping("/{bookingId}/rating")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingRatingResponseDto>> getBookingRating(
            @PathVariable Long bookingId) {
        BookingRatingResponseDto rating = bookingService.getBookingRating(bookingId);
        return ResponseEntity.ok(ApiResponse.<BookingRatingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(rating)
                .build());
    }

}
