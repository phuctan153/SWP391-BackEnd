package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.invoice.InvoiceSummaryDTO;
import com.example.ev_rental_backend.dto.payment.PaymentInitRequestDTO;
import com.example.ev_rental_backend.dto.payment.PaymentResponseDTO;
import com.example.ev_rental_backend.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API khởi tạo thanh toán
     * POST /api/payments/init
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> initPayment(
            @Valid @RequestBody PaymentInitRequestDTO requestDTO) {

        PaymentResponseDTO paymentData = paymentService.initPayment(requestDTO);

        ApiResponse<PaymentResponseDTO> response = ApiResponse.<PaymentResponseDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(paymentData)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * API lấy thông tin invoice
     * GET /api/payments/invoice/{invoiceId}
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceSummaryDTO>> getInvoiceSummary(
            @PathVariable Long invoiceId) {

        InvoiceSummaryDTO invoiceData = paymentService.getInvoiceSummary(invoiceId);

        ApiResponse<InvoiceSummaryDTO> response = ApiResponse.<InvoiceSummaryDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(invoiceData)
                .build();

        return ResponseEntity.ok(response);
    }

}
