package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.repository.NotificationRepository;
import com.example.ev_rental_backend.service.booking.BookingService;
import com.example.ev_rental_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final JwtTokenUtil jwtTokenUtil;
    private final NotificationRepository notificationRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {

        // Cắt "Bearer " khỏi header
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // Lấy thông tin từ token
        Long userId = jwtTokenUtil.extractUserId(token);
        String role = jwtTokenUtil.extractRole(token);
        Notification.RecipientType recipientType = Notification.RecipientType.valueOf(role.toUpperCase());

        // Lấy danh sách thông báo từ DB
        List<Notification> notifications =
                notificationRepository.findByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(recipientType, userId);

        // Trả về response chuẩn
        return ResponseEntity.ok(
                ApiResponse.<List<Notification>>builder()
                        .status("success")
                        .code(200)
                        .data(notifications)
                        .build()
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<?>> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long notificationId) {

        // Cắt "Bearer " khỏi header
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // Lấy userId và role từ token
        Long userId = jwtTokenUtil.extractUserId(token);
        String role = jwtTokenUtil.extractRole(token);
        Notification.RecipientType recipientType = Notification.RecipientType.valueOf(role.toUpperCase());

        // Tìm thông báo trong DB
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo #" + notificationId));

        // Kiểm tra quyền — chỉ người nhận mới được đánh dấu là đọc
        if (!notification.getRecipientId().equals(userId)
                || notification.getRecipientType() != recipientType) {
            throw new RuntimeException("Bạn không có quyền đánh dấu thông báo này.");
        }

        // Cập nhật trạng thái
        notification.setIsRead(true);
        notificationRepository.save(notification);

        // Trả response chuẩn
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(200)
                        .data("Thông báo #" + notificationId + " đã được đánh dấu là đã đọc.")
                        .build()
        );
    }

    @PostMapping("/booking/{bookingId}/cash-payment")
    @PreAuthorize("hasRole('RENTER')")
    public ResponseEntity<ApiResponse<?>> notifyCashPaymentBooking(
            @PathVariable Long bookingId) {

        Booking booking = bookingService.getBookingEntityById(bookingId);
        notificationService.notifyStationAdminsForCashPayment(booking);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(String.format("Đã gửi thông báo đến Station Admin tại trạm %s.",
                                booking.getVehicle().getStation().getName()))
                        .build()
        );
    }





}
