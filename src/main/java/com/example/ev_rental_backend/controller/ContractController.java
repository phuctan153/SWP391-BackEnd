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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

@Slf4j
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
    @GetMapping("/contracts/{bookingId}")
    public ResponseEntity<ApiResponse<?>> getContractByBookingId(
            @PathVariable Long bookingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 🔐 Giải mã JWT
            String token = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractUserId(token);
            String role = jwtTokenUtil.extractRole(token);

            log.info("🔑 Token decoded: userId={}, role={}", userId, role);

            // 🧩 Lấy thông tin contract theo bookingId
            ContractResponseDTO dto = contractService.getContractByBookingId(bookingId);
            if (dto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<String>builder()
                                .status("error").code(404)
                                .data("Không tìm thấy hợp đồng của booking #" + bookingId)
                                .build());
            }

            // 🧩 Lấy thông tin booking để kiểm tra quyền
            BookingResponseDto booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<String>builder()
                                .status("error").code(404)
                                .data("Không tìm thấy thông tin booking #" + bookingId)
                                .build());
            }

            Long renterId = booking.getRenterId();
            Long staffReceiveId = booking.getStaffReceiveId();
            Long staffReturnId = booking.getStaffReturnId();

            boolean isRenter = role.equalsIgnoreCase("RENTER");
            boolean isStaff = role.equalsIgnoreCase("STAFF");
            boolean isAdmin = role.equalsIgnoreCase("ADMIN");

            // ✅ Nếu là ADMIN → luôn có quyền truy cập
            if (isAdmin) {
                log.info("👑 Admin (userId={}) truy cập hợp đồng bookingId={}", userId, bookingId);
                return ResponseEntity.ok(ApiResponse.<ContractResponseDTO>builder()
                        .status("success").code(200).data(dto).build());
            }

            // ✅ Renter chỉ xem được hợp đồng của mình
            if (isRenter && !renterId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.<String>builder()
                                .status("error").code(403)
                                .data("Bạn không có quyền xem hợp đồng này").build());
            }

            // ✅ Staff chỉ được xem nếu là người giao hoặc nhận xe
            if (isStaff) {
                Long staffCreatorId = dto.getCreatedByStaffId(); // 🆕 thêm dòng này

                boolean isAuthorizedStaff =
                        (staffReceiveId != null && staffReceiveId.equals(userId)) ||
                                (staffReturnId != null && staffReturnId.equals(userId)) ||
                                (staffCreatorId != null && staffCreatorId.equals(userId)); // 🆕 thêm quyền cho staff tạo hợp đồng

                if (!isAuthorizedStaff) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.<String>builder()
                                    .status("error").code(403)
                                    .data("Bạn không có quyền xem hợp đồng này (không phụ trách booking này)").build());
                }
            }

            // ✅ Nếu qua hết kiểm tra → cho phép trả về contract
            return ResponseEntity.ok(ApiResponse.<ContractResponseDTO>builder()
                    .status("success").code(200).data(dto).build());

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error").code(400).data(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error").code(500)
                    .data("Lỗi hệ thống: " + e.getMessage()).build());
        }
    }



    // 🧾 Renter / Staff / Admin: Xem file hợp đồng PDF an toàn
    @GetMapping("/contracts/view/{contractId}")
    public ResponseEntity<?> viewContractFile(
            @PathVariable Long contractId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 🧩 Giải mã token

            String token = authHeader.substring(7);
            Long userId = jwtTokenUtil.extractUserId(token);
            String role = jwtTokenUtil.extractRole(token);
            log.info("🔑 Token decoded: userId={}, role={}", userId, role);



            // 🧾 Lấy thông tin hợp đồng
            ContractResponseDTO contract = contractService.getContractById(contractId);
            if (contract == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("Không tìm thấy hợp đồng").build());
            }

            // 🧾 Lấy thông tin booking liên quan
            BookingResponseDto booking = bookingService.getBookingById(contract.getBookingId());
            if (booking == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("Không tìm thấy thông tin booking liên quan").build());
            }

            // 🧍 ID người thuê và nhân viên liên quan
            Long renterId = booking.getRenterId();
            Long staffReceiveId = booking.getStaffReceiveId();
            Long staffReturnId = booking.getStaffReturnId();
            Long staffCreatorId = contract.getCreatedByStaffId(); // ⚙️ cần có trong ContractResponseDTO

            boolean isRenter = "RENTER".equalsIgnoreCase(role);
            boolean isStaff = "STAFF".equalsIgnoreCase(role);
            boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
            if ("ADMIN".equalsIgnoreCase(role)) {
                String filePath = "uploads/contracts/contract_" + contractId + ".pdf";
                File file = new File(filePath);

                if (!file.exists()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.builder().status("error").code(404)
                                    .data("File hợp đồng không tồn tại").build());
                }

                FileSystemResource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getName())
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            }

            // ✅ Renter chỉ xem được hợp đồng của mình
            if (isRenter && !renterId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.builder().status("error").code(403)
                                .data("Bạn không có quyền xem hợp đồng này").build());
            }

            // ✅ Staff chỉ được xem nếu là người tạo, giao, hoặc nhận xe
            if (isStaff) {
                boolean authorized =
                        (staffCreatorId != null && staffCreatorId.equals(userId)) ||
                                (staffReceiveId != null && staffReceiveId.equals(userId)) ||
                                (staffReturnId != null && staffReturnId.equals(userId));

                if (!authorized) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.builder().status("error").code(403)
                                    .data("Bạn không có quyền xem hợp đồng này (không thuộc nhóm staff phụ trách)").build());
                }
            }
            log.info("📄 Contract ID: {}", contractId);
            log.info("👤 Renter ID: {}", booking.getRenterId());
            log.info("👷 Staff Creator ID: {}", contract.getCreatedByStaffId());
            log.info("🚗 Staff Receive ID: {}", booking.getStaffReceiveId());
            log.info("🚗 Staff Return ID: {}", booking.getStaffReturnId());



            // 📂 Kiểm tra file tồn tại
            String filePath = "uploads/contracts/contract_" + contractId + ".pdf";
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.builder().status("error").code(404)
                                .data("File hợp đồng không tồn tại").build());
            }



            // 📄 Trả file PDF inline
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
