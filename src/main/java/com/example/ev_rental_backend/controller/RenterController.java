package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.BookingResponseDto;
import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.IdentityDocument;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.mapper.RenterMapper;
import com.example.ev_rental_backend.repository.IdentityDocumentRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.service.booking.BookingService;
import com.example.ev_rental_backend.service.renter.RenterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renter")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
public class RenterController {

    private final BookingService bookingService;
    private final JwtTokenUtil jwtTokenUtil;
    private final RenterRepository renterRepository;
    private final RenterMapper renterMapper;
    private final IdentityDocumentRepository identityDocumentRepository;

    private final RenterService renterService;

    // 🟢 Lấy thông tin cá nhân của renter dựa trên JWT token
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(HttpServletRequest request) {
        try {
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

            String token = authHeader.substring(7);
            String email = jwtTokenUtil.extractEmail(token);

            Renter renter = renterRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người thuê"));

            RenterResponseDTO responseDTO = renterMapper.toResponseDto(renter);
            responseDTO.setKycStatus(renterService.getKycStatusForRenter(renter));

            if (renter.getWallet() != null) {
                responseDTO.setWalletId(renter.getWallet().getWalletId());
            }

            // Lấy CCCD và GPLX của renter
            List<IdentityDocument> docs = identityDocumentRepository.findByRenter(renter);
            for (IdentityDocument doc : docs) {
                RenterResponseDTO.IdentityDocDTO docDTO = RenterResponseDTO.IdentityDocDTO.builder()
                        .documentNumber(doc.getDocumentNumber())
                        .fullName(doc.getFullName())
                        .type(doc.getType().name())
                        .status(doc.getStatus().name())
                        .issueDate(doc.getIssueDate())
                        .expiryDate(doc.getExpiryDate())
                        .verifiedAt(doc.getVerifiedAt())
                        .build();

                if (doc.getType() == IdentityDocument.DocumentType.NATIONAL_ID) {
                    responseDTO.setCccd(docDTO);
                } else if (doc.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE) {
                    responseDTO.setGplx(docDTO);
                }
            }

            responseDTO.setKycStatus(renterService.getKycStatusForRenter(renter));

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


    @PostMapping("/verify-kyc")
    public ResponseEntity<ApiResponse<?>> verifyKyc(@RequestBody @Valid KycVerificationDTO dto) {
        try {
            // 1. Gọi service xử lý xác thực KYC
            Renter verified = renterService.verifyKyc(dto);

            // 2. Chuyển entity sang DTO (để tránh leak dữ liệu)
            RenterResponseDTO renterDto = renterService.toResponseDto(verified);

            // 3. Bổ sung thông tin trạng thái KYC
            String kycStatus = renterService.getKycStatusForRenter(verified);
            renterDto.setKycStatus(kycStatus);

            // 4. Trả về phản hồi dạng chuẩn
            return ResponseEntity.ok(
                    ApiResponse.<RenterResponseDTO>builder()
                            .status("success")
                            .code(200)
                            .data(renterDto)
                            .build()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(500)
                            .data("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }

    /**
     * GET /api/renters/me/bookings
     * Lấy tất cả booking của renter hiện tại đang đăng nhập
     */
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> getMyBookings(
            @RequestParam(required = false) String status) {

        List<BookingResponseDto> bookings = bookingService.getMyBookings(status);

        return ResponseEntity.ok(ApiResponse.<List<BookingResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(bookings)
                .build());
    }

    /**
     * GET /api/renters/bookings/{bookingId}
     * Lấy chi tiết 1 booking của renter
     */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponseDto>> getMyBookingDetail(
            @PathVariable Long bookingId) {

        BookingResponseDto booking = bookingService.getMyBookingDetail(bookingId);

        return ResponseEntity.ok(ApiResponse.<BookingResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(booking)
                .build());
    }
}
