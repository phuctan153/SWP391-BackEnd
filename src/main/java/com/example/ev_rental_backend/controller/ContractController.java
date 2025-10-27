package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.contract.AdminContractSignDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;
import com.example.ev_rental_backend.entity.TermCondition;
import com.example.ev_rental_backend.service.contract.ContractService;
import com.example.ev_rental_backend.service.contract.TermTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContractController {

    private final TermTemplateService termTemplateService;
    private final ContractService contractService;

    /**
     * 📄 API: Lấy mẫu điều khoản hợp đồng (cho Staff xem trước)
     */
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

    /**
     * 📝 API: Staff tạo hợp đồng mới
     */
    @PostMapping("/staff/contracts/create")
    public ResponseEntity<ApiResponse<?>> createContract(@RequestBody ContractRequestDTO dto) {
        try {
            ContractResponseDTO contract = contractService.createContract(dto);
            return ResponseEntity.ok(
                    ApiResponse.<ContractResponseDTO>builder()
                            .status("success")
                            .code(HttpStatus.OK.value())
                            .data(contract)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(HttpStatus.BAD_REQUEST.value())
                            .data(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .data("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/staff/contracts/{contractId}/send-to-admin")
    public ResponseEntity<ApiResponse<?>> sendContractToAdmin(@PathVariable Long contractId) {
        try {
            contractService.sendContractToAdmin(contractId);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .status("success")
                            .code(200)
                            .data("Đã gửi hợp đồng lên Admin thành công.")
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

    @GetMapping("/admin/contracts")
    public ResponseEntity<ApiResponse<?>> getContractsByStatus(
            @RequestParam(required = false, defaultValue = "PENDING_ADMIN_SIGNATURE") String status) {
        try {
            List<BookingContractInfoDTO> list = contractService.getContractsByStatus(status);
            return ResponseEntity.ok(
                    ApiResponse.<List<BookingContractInfoDTO>>builder()
                            .status("success")
                            .code(200)
                            .data(list)
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
        }
    }

    @PostMapping("/admin/contracts/{contractId}/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(
            @PathVariable Long contractId,
            @RequestParam Long adminId) {

        try {
            contractService.sendOtpForAdminSignature(contractId, adminId);

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .status("success")
                            .code(200)
                            .data("✅ Mã OTP đã được gửi đến email quản trị viên.")
                            .build()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data("Lỗi gửi OTP: " + e.getMessage())
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

    @PostMapping("/admin/contracts/verify-sign")
    public ResponseEntity<ApiResponse<?>> verifySign(@RequestBody AdminContractSignDTO dto) {
        try {
            contractService.verifyAdminSignature(dto);

            String msg = dto.isApproved()
                    ? "✅ Hợp đồng đã được ký và renter đã được thông báo."
                    : "❌ Hợp đồng đã bị từ chối. Booking sẽ bị hủy và renter sẽ được hoàn tiền cọc.";

            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .status("success")
                            .code(200)
                            .data(msg)
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

    @PostMapping("/renter/contracts/send-otp")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestParam Long bookingId) {
        try {
            contractService.sendOtpToRenter(bookingId);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Mã OTP đã được gửi đến email của bạn.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.<String>builder()
                    .status("error")
                    .code(500)
                    .data("Lỗi gửi OTP: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/staff/contracts/verify-sign")
    public ResponseEntity<ApiResponse<?>> verifySign(
            @RequestParam Long bookingId,
            @RequestParam String otpCode) {
        try {
            contractService.verifyRenterSignature(bookingId, otpCode);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .status("success")
                    .code(200)
                    .data("Hợp đồng đã được ký thành công.")
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/renter/contracts/{bookingId}")
    public ResponseEntity<ApiResponse<?>> getContractByBookingId(@PathVariable Long bookingId) {
        try {
            ContractResponseDTO dto = contractService.getContractByBookingId(bookingId);
            return ResponseEntity.ok(ApiResponse.<ContractResponseDTO>builder()
                    .status("success")
                    .code(200)
                    .data(dto)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .status("error")
                    .code(400)
                    .data(e.getMessage())
                    .build());
        }
    }





}
