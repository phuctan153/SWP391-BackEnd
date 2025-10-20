package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
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
@RequestMapping("/api/staff/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final TermTemplateService termTemplateService;
    private final ContractService contractService;

    /**
     * 📄 API: Lấy mẫu điều khoản hợp đồng (cho Staff xem trước)
     */
    @GetMapping("/template")
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


    @GetMapping("/booking-info/{bookingId}")
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
    @PostMapping("/create")
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

    @PostMapping("/{contractId}/send-to-admin")
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


}
