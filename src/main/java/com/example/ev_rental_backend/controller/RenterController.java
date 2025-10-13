package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.mapper.RenterMapper;
import com.example.ev_rental_backend.repository.RenterRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/renter")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RenterController {

    private final JwtTokenUtil jwtTokenUtil;
    private final RenterRepository renterRepository;
    private final RenterMapper renterMapper;

    // 🟢 Lấy thông tin cá nhân của renter dựa trên JWT token
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(HttpServletRequest request) {
        try {
            // 🔹 Lấy header Authorization
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(403).body(
                        ApiResponse.builder()
                                .status("error")
                                .code(403)
                                .data("Thiếu hoặc sai định dạng Authorization header")
                                .build()
                );
            }

            // 🔹 Cắt "Bearer " để lấy token thật
            String token = authHeader.substring(7);

            // 🔹 Trích xuất email từ token
            String email = jwtTokenUtil.extractEmail(token);

            // 🔹 Tìm renter trong database
            Renter renter = renterRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người thuê"));

            // 🔹 Map sang DTO phản hồi
            RenterResponseDTO responseDTO = renterMapper.toResponseDto(renter);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .code(200)
                    .data(responseDTO)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.builder()
                            .status("error")
                            .code(401)
                            .data("Token không hợp lệ hoặc đã hết hạn: " + e.getMessage())
                            .build()
            );
        }
    }
}
