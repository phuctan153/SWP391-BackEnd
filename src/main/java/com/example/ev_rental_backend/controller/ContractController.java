package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.booking.BookingResponseDto;
import com.example.ev_rental_backend.dto.contract.AdminContractSignDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;
import com.example.ev_rental_backend.entity.TermCondition;
import com.example.ev_rental_backend.service.booking.BookingService;
import com.example.ev_rental_backend.service.contract.ContractService;
import com.example.ev_rental_backend.service.contract.TermTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContractController {

    private final TermTemplateService termTemplateService;
    private final ContractService contractService;
    private final JwtTokenUtil jwtTokenUtil;
    private final BookingService bookingService;

    // 📄 Staff: Lấy mẫu điều khoản hợp đồng
    @GetMapping("/staff/contracts/template")
    public ResponseEntity<ApiResponse<List<TermCondition>>> getContractTemplate() {
        List<TermCondition> terms = termTemplateService.loadDefaultTerms();
        return ResponseEntity.ok(
                ApiResponse.<List<TermCondition>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(terms)
                        .build()
        );
    }

    // 📄 Staff: Lấy thông tin booking để tạo hợp đồng
    @GetMapping("/staff/contracts/booking-info/{bookingId}")
    public ResponseEntity<ApiResponse<BookingContractInfoDTO>> getBookingInfoForContract(@PathVariable Long bookingId) {
        BookingContractInfoDTO info = contractService.getBookingInfoForContract(bookingId);
        return ResponseEntity.ok(
                ApiResponse.<BookingContractInfoDTO>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(info)
                        .build()
        );
    }

    // 📝 Staff: Tạo hợp đồng mới
    @PostMapping("/staff/contracts/create")
    public ResponseEntity<ApiResponse<?>> createContract(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ContractRequestDTO dto) {
        try {
            String token = authHeader.substring(7);
            Long staffId = jwtTokenUtil.extractUserId(token);
            ContractResponseDTO contract = contractService.createContract(dto, staffId);
            return ResponseEntity.ok(
                    ApiResponse.<ContractResponseDTO>builder()
                            .status("success")
                            .code(200)
                            .data(contract)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500).data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }

    // 📤 Staff: Gửi hợp đồng lên admin
    @PostMapping("/staff/contracts/{contractId}/send-to-admin")
    public ResponseEntity<ApiResponse<?>> sendContractToAdmin(@PathVariable Long contractId) {
        try {
            contractService.sendContractToAdmin(contractId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success").code(200)
                    .data("Đã gửi hợp đồng lên Admin thành công.").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500)
                    .data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }

    // 👩‍💼 Admin: Lấy danh sách hợp đồng theo trạng thái
    @GetMapping("/admin/contracts")
    public ResponseEntity<ApiResponse<?>> getContractsByStatus(
            @RequestParam(required = false, defaultValue = "PENDING_ADMIN_SIGNATURE") String status) {
        try {
            List<BookingContractInfoDTO> list = contractService.getContractsByStatus(status);
            return ResponseEntity.ok(ApiResponse.<List<BookingContractInfoDTO>>builder()
                    .status("success").code(200).data(list).build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        }
    }

    // 👩‍💼 Admin: Gửi OTP ký hợp đồng
    @PostMapping("/admin/contracts/{contractId}/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(
            @PathVariable Long contractId, @RequestParam Long adminId) {
        try {
            contractService.sendOtpForAdminSignature(contractId, adminId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success").code(200)
                    .data("✅ Mã OTP đã được gửi đến email quản trị viên.").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400)
                    .data("Lỗi gửi OTP: " + e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500)
                    .data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }

    // 👩‍💼 Admin: Xác minh ký hợp đồng
    @PostMapping("/admin/contracts/verify-sign")
    public ResponseEntity<ApiResponse<?>> verifySign(@RequestBody AdminContractSignDTO dto) {
        try {
            contractService.verifyAdminSignature(dto);
            String msg = dto.isApproved()
                    ? "✅ Hợp đồng đã được ký và renter đã được thông báo."
                    : "❌ Hợp đồng bị từ chối. Booking bị hủy và hoàn tiền cọc.";
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success").code(200).data(msg).build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500).data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }

    // 🧍‍♂️ Renter: Gửi OTP ký hợp đồng
    @PostMapping("/renter/contracts/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestParam Long bookingId) {
        try {
            contractService.sendOtpToRenter(bookingId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success").code(200)
                    .data("Mã OTP đã được gửi đến email của bạn.").build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500)
                    .data("Lỗi gửi OTP: " + e.getMessage()).build());
        }
    }

    // 🧍‍♂️ Renter: Xác minh ký hợp đồng bằng OTP
    @PostMapping("/renter/contracts/verify-sign")
    public ResponseEntity<ApiResponse<?>> verifyRenterSign(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Long bookingId,
            @RequestParam String otpCode) {
        try {
            String token = authHeader.substring(7);
            Long renterId = jwtTokenUtil.extractUserId(token);
            contractService.verifyRenterSignature(bookingId, renterId, otpCode);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success").code(200)
                    .data("✅ Hợp đồng đã được ký thành công.").build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500)
                    .data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }

    // 🧍‍♂️ Renter: Lấy hợp đồng theo Booking ID
    @GetMapping("/renter/contracts/{bookingId}")
    public ResponseEntity<ApiResponse<?>> getContractByBookingId(@PathVariable Long bookingId) {
        try {
            ContractResponseDTO dto = contractService.getContractByBookingId(bookingId);
            return ResponseEntity.ok(ApiResponse.<ContractResponseDTO>builder()
                    .status("success").code(200).data(dto).build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        }
    }

    // 🧾 Renter / Staff / Admin: Xem file hợp đồng PDF an toàn
    @GetMapping("/renter/contracts/view/{contractId}")
    public ResponseEntity<?> viewContractFile(
            @PathVariable Long contractId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 🧩 Giải mã token
            String token = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractUserId(token);
            String role = jwtTokenUtil.extractRole(token);

            // 🧩 Lấy thông tin hợp đồng
            ContractResponseDTO contract = contractService.getContractById(contractId);
            if (contract == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("Không tìm thấy hợp đồng").build());
            }

            // 🧩 Lấy thông tin booking để biết renterId và staffId
            BookingResponseDto booking = bookingService.getBookingById(contract.getBookingId());
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("Không tìm thấy thông tin booking liên quan").build());
            }

            Long renterId = booking.getRenterId();
            Long staffId = booking.getStaffId(); // cần có field này trong BookingResponseDto

            // 🧩 Kiểm tra quyền truy cập
            boolean isRenter = "RENTER".equalsIgnoreCase(role);
            boolean isStaff = "STAFF".equalsIgnoreCase(role);
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

            // ✅ Chỉ Renter chính chủ mới xem được
            if (isRenter && !renterId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.builder().status("error").code(403)
                                .data("Bạn không có quyền xem hợp đồng này").build());
            }

            // ✅ Staff chỉ được xem hợp đồng thuộc booking mà họ xử lý
            if (isStaff && (staffId == null || !staffId.equals(userId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.builder().status("error").code(403)
                                .data("Bạn không có quyền xem hợp đồng này vì không phụ trách booking này").build());
            }

            // ✅ Admin có quyền xem tất cả
            // (Không cần kiểm tra thêm gì)

            // 🧩 Kiểm tra file PDF tồn tại
            String filePath = "uploads/contracts/contract_" + contractId + ".pdf";
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("File hợp đồng không tồn tại").build());
            }

            // 🧩 Trả file PDF inline
            FileSystemResource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.builder().status("error").code(500)
                            .data("Lỗi khi truy cập hợp đồng: " + e.getMessage()).build());
        }
    }

}
