package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final JwtTokenUtil jwtTokenUtil;
    private final NotificationRepository notificationRepository;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyNotifications(
            @RequestHeader("Authorization") String authHeader) {

        // 1️⃣ Cắt "Bearer " khỏi header
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // 2️⃣ Lấy thông tin từ token
        Long userId = jwtTokenUtil.extractUserId(token);
        String role = jwtTokenUtil.extractRole(token);
        Notification.RecipientType recipientType = Notification.RecipientType.valueOf(role.toUpperCase());

        // 3️⃣ Lấy danh sách thông báo từ DB
        List<Notification> notifications =
                notificationRepository.findByRecipientTypeAndRecipientIdOrderByNotificationIdDesc(recipientType, userId);

        // 4️⃣ Trả về response chuẩn
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

        // 1️⃣ Cắt "Bearer " khỏi header
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // 2️⃣ Lấy userId và role từ token
        Long userId = jwtTokenUtil.extractUserId(token);
        String role = jwtTokenUtil.extractRole(token);
        Notification.RecipientType recipientType = Notification.RecipientType.valueOf(role.toUpperCase());

        // 3️⃣ Tìm thông báo trong DB
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo #" + notificationId));

        // 4️⃣ Kiểm tra quyền — chỉ người nhận mới được đánh dấu là đọc
        if (!notification.getRecipientId().equals(userId)
                || notification.getRecipientType() != recipientType) {
            throw new RuntimeException("Bạn không có quyền đánh dấu thông báo này.");
        }

        // 5️⃣ Cập nhật trạng thái
        notification.setIsRead(true);
        notificationRepository.save(notification);

        // 6️⃣ Trả response chuẩn
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .code(200)
                        .data("Thông báo #" + notificationId + " đã được đánh dấu là đã đọc.")
                        .build()
        );
    }

}
