package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.*;
import com.example.ev_rental_backend.entity.Admin;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.repository.AdminRepository;
import com.example.ev_rental_backend.entity.BookingImage;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.service.booking.BookingService;
import com.example.ev_rental_backend.service.notification.NotificationService;
import com.example.ev_rental_backend.service.policy.PolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final JwtTokenUtil jwtTokenUtil;
    private final NotificationService notificationService;
    private final AdminRepository adminRepository;
    private final PolicyService policyService;

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

        //thêm phần gửi email khi ADMIN từ chối booking
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().contains("ADMIN"))){
            //gửi email cho renter
            bookingService.sendCancellationEmailToRenter(bookingId);

            //thông báo hoàn tiền cọc cho admin
            Long adminId = jwtTokenUtil.extractUserId(
                    auth.getCredentials().toString()); // hoặc token từ Header nếu bạn lưu JWT ở đây
            notificationService.sendNotificationToAdmin(
                    adminId,
                    "💰 Hoàn cọc sau khi hủy booking",
                    String.format("Bạn đã hủy booking #%d — cần hoàn tiền đặt cọc cho renter.",
                            bookingId)
            );
        }
        else if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("RENTER"))) {

            double refundPercent = policyService.getPolicyValue(Policy.PolicyType.REFUND_PERCENT_RENTER);

            //gửi thông báo cho admin kích hoạt hoàn tiền
            Admin admin = adminRepository.findFirstByStatus(Admin.Status.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy admin đang hoạt động"));
            notificationService.sendNotificationToAdmin(
                    admin.getGlobalAdminId(),
                    "📩 Renter hủy đặt xe",
                    String.format(
                            "Renter đã hủy booking #%d — cần hoàn %.0f%% tiền cọc theo chính sách doanh nghiệp.",
                            bookingId, refundPercent
                    )
            );
        }

        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    /**
     * GET /api/bookings/{bookingId}/confirm-cancel - Xác nhận trước khi hủy booking
     * Hiển thị phần trăm hoàn tiền theo chính sách doanh nghiệp
     */
    @GetMapping("/{bookingId}/confirm-cancel")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmCancelBooking(@PathVariable Long bookingId) {
        double refundPercent = policyService.getPolicyValue(Policy.PolicyType.REFUND_PERCENT_RENTER);
        double depositAmount = policyService.getPolicyValue(Policy.PolicyType.DEPOSIT_AMOUNT);
        Map<String, Object> data = getStringObjectMap(bookingId, depositAmount, refundPercent);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(data)
                .build());
    }

    private static Map<String, Object> getStringObjectMap(Long bookingId, double depositAmount, double refundPercent) {
        double refundAmount = depositAmount * (refundPercent / 100.0);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", bookingId);
        data.put("refundPercent", refundPercent);
        data.put("depositAmount", depositAmount);
        data.put("refundAmount", refundAmount);
        data.put("message", String.format(
                "Nếu bạn hủy booking, hệ thống sẽ hoàn %.0f%% tiền cọc (≈ %.0f VND) theo chính sách hiện tại.",
                refundPercent, refundAmount
        ));
        return data;
    }

    /**
     * GET /api/bookings/station/contracts
     * Lấy danh sách booking theo trạm mà staff đang hoạt động, kèm thông tin hợp đồng
     */
    @GetMapping("/station/contracts")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<BookingWithContractDTO>>> getBookingsWithContractsByActiveStation(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long staffId = jwtTokenUtil.extractUserId(token);

        List<BookingWithContractDTO> bookings = bookingService.getBookingsWithContractsByActiveStation(staffId);

        return ResponseEntity.ok(ApiResponse.<List<BookingWithContractDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Danh sách booking và hợp đồng tại trạm bạn đang hoạt động")
                .data(bookings)
                .build());
    }


    // ==================== 5.2. Booking Images ====================

    @PostMapping(
            value = "/{bookingId}/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingImageResponseDto>> uploadBookingImage(
            @PathVariable Long bookingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageType") String imageType,
            @RequestParam(value = "vehicleComponent", required = false) String vehicleComponent,
            @RequestParam(value = "description", required = false) String description) {

        BookingImageResponseDto image = bookingService.uploadBookingImage(
                bookingId, file, imageType, vehicleComponent, description);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingImageResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .message("Image uploaded successfully")
                        .data(image)
                        .build());
    }

    @PatchMapping("/images/{imageId}/confirm")
    @PreAuthorize("hasAnyRole('RENTER')")
    public ResponseEntity<ApiResponse<BookingImageResponseDto>> confirmBookingImage(
            @PathVariable Long imageId) {

        BookingImageResponseDto image = bookingService.confirmBookingImage(imageId);

        return ResponseEntity.ok(
                ApiResponse.<BookingImageResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .message("Image confirmed successfully")
                        .data(image)
                        .build()
        );
    }

    /**
     * GET /api/v1/bookings/{bookingId}/images - Lấy danh sách ảnh với filter
     *
     * @param bookingId ID booking
     * @param imageType Filter theo loại ảnh (optional)
     * @param vehicleComponent Filter theo hạng mục xe (optional)
     */
    @GetMapping("/{bookingId}/images")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingImageResponseDto>>> getBookingImages(
            @PathVariable Long bookingId,
            @RequestParam(value = "imageType", required = false) String imageType,
            @RequestParam(value = "vehicleComponent", required = false) String vehicleComponent) {

        List<BookingImageResponseDto> images = bookingService.getBookingImages(
                bookingId, imageType, vehicleComponent);

        return ResponseEntity.ok(ApiResponse.<List<BookingImageResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(images)
                .build());
    }

    /**
     * DELETE /api/v1/bookings/{bookingId}/images/{imageId} - Xóa ảnh
     */
    @DeleteMapping("/{bookingId}/images/{imageId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBookingImage(
            @PathVariable Long bookingId,
            @PathVariable Long imageId) {

        bookingService.deleteBookingImage(bookingId, imageId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Image deleted successfully")
                .build());
    }

    /**
     * GET /api/v1/bookings/{bookingId}/images/checklist - Kiểm tra checklist ảnh
     *
     * @param bookingId ID booking
     * @param imageType BEFORE_RENTAL hoặc AFTER_RENTAL
     */
    @GetMapping("/{bookingId}/images/checklist")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkImageChecklist(
            @PathVariable Long bookingId,
            @RequestParam("imageType") String imageType) {

        BookingImage.ImageType type;
        try {
            type = BookingImage.ImageType.valueOf(imageType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid image type: " + imageType, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> checklist = bookingService.checkImageChecklist(bookingId, type);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(checklist)
                .build());
    }

    /**
     * GET /api/v1/bookings/image-types - Lấy danh sách loại ảnh
     */
    @GetMapping("/image-types")
    public ResponseEntity<ApiResponse<List<String>>> getImageTypes() {
        List<String> types = Arrays.stream(BookingImage.ImageType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(types)
                .build());
    }

    /**
     * GET /api/v1/bookings/vehicle-components - Lấy danh sách hạng mục xe
     */
    @GetMapping("/vehicle-components")
    public ResponseEntity<ApiResponse<List<String>>> getVehicleComponents() {
        List<String> components = Arrays.stream(BookingImage.VehicleComponent.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(components)
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
            @PathVariable Long bookingId,
            HttpServletRequest request) { // ✅ thêm request để lấy token

        BookingResponseDto booking = bookingService.updateStatusToInUse(bookingId, request);

        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }



    //admin đồng ý cho thuê
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{bookingId}/status/reserved")
    public ResponseEntity<ApiResponse<BookingResponseDto>> updateStatusToReserved(
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.updateStatusToReserved(bookingId);
        Booking bookingEntity = bookingService.getBookingEntityById(bookingId);
        notificationService.notifyStationAdminsToCreateContract(bookingEntity);
        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }

    // ==================== 5.4. Return Process ====================
    //renter thông báo trả xe
    @PutMapping("/{bookingId}/notify-return")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<String>> notifyReturnRequest(
            @PathVariable Long bookingId,
            HttpServletRequest request) {

        // ✅ Lấy header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException("Thiếu hoặc sai định dạng Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Lấy token từ header
        String token = authHeader.substring(7);

        // ✅ Kiểm tra token hợp lệ
        if (!jwtTokenUtil.isTokenValid(token)) {
            throw new CustomException("Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED);
        }

        // ✅ Lấy email từ token
        String renterEmail = jwtTokenUtil.extractEmail(token);

        // ✅ Gửi thông báo tới tất cả staff của trạm
        bookingService.notifyStationStaffAboutReturn(bookingId, renterEmail);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Thông báo đã được gửi đến các nhân viên của trạm.")
                .data("Return notification sent successfully.")
                .build());
    }



    /**
     * POST /api/bookings/{bookingId}/return - Trả xe (BR-11, BR-12, BR-13, BR-14, BR-15, BR-26, BR-27)
     * //có thể xóa
     */
    @PostMapping("/{bookingId}/return")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReturnResponseDto>> returnVehicle(
            @PathVariable Long bookingId,
            @Valid @RequestBody ReturnRequestDto requestDto,
            HttpServletRequest request) { // ✅ thêm request để lấy token

        ReturnResponseDto response = bookingService.returnVehicle(bookingId, requestDto, request);

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

    /**
     * GET /api/bookings/admin/all - Admin xem tất cả booking trong hệ thống
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getAllBookingsForAdmin() {
        try {
            List<BookingResponseDto> bookings = bookingService.getAllBookings();

            return ResponseEntity.ok(ApiResponse.<List<BookingResponseDto>>builder()
                    .status("success")
                    .code(HttpStatus.OK.value())
                    .message("Danh sách toàn bộ booking trong hệ thống (Admin truy cập)")
                    .data(bookings)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<BookingResponseDto>>builder()
                            .status("error")
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi khi lấy danh sách booking: " + e.getMessage())
                            .build());
        }
    }




}
