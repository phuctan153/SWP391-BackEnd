package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.invoice.*;
import com.example.ev_rental_backend.service.invoice.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    /**
     * GET /api/invoices/{invoiceId} - Chi tiết hóa đơn
     */
    @GetMapping("/invoices/{invoiceId}")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> getInvoiceById(@PathVariable Long invoiceId) {
        InvoiceResponseDto invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.<InvoiceResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(invoice)
                .build());
    }

    /**
     * GET /api/bookings/{bookingId}/invoices - Danh sách hóa đơn của booking
     */
    @GetMapping("/bookings/{bookingId}/invoices")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDto>>> getInvoicesByBookingId(
            @PathVariable Long bookingId) {
        List<InvoiceResponseDto> invoices = invoiceService.getInvoicesByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.<List<InvoiceResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(invoices)
                .build());
    }

    /**
     * POST /api/bookings/{bookingId}/invoices/deposit - Tạo hóa đơn đặt cọc (BR-06, BR-23)
     */
    @PostMapping("/bookings/{bookingId}/invoices/deposit")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> createDepositInvoice(
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateDepositInvoiceDto requestDto) {
        InvoiceResponseDto invoice = invoiceService.createDepositInvoice(bookingId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(invoice)
                        .build());
    }

    /**
     * POST /api/bookings/{bookingId}/invoices/final - Tạo hóa đơn cuối (BR-27)
     */
    @PostMapping("/bookings/{bookingId}/invoices/final")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> createFinalInvoice(
            @PathVariable Long bookingId,
            @Valid @RequestBody CreateFinalInvoiceDto requestDto) {
        InvoiceResponseDto invoice = invoiceService.createFinalInvoice(bookingId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(invoice)
                        .build());
    }

    // 7.2. Invoice Details

    /**
     * POST /api/invoices/{invoiceId}/details - Thêm dòng chi phí (phụ tùng, phạt) (BR-13)
     */
    @PostMapping("/invoices/{invoiceId}/details")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceDetailResponseDto>> addInvoiceDetail(
            @PathVariable Long invoiceId,
            @Valid @RequestBody CreateInvoiceDetailDto requestDto) {
        InvoiceDetailResponseDto detail = invoiceService.addInvoiceDetail(invoiceId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceDetailResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(detail)
                        .build());
    }

    /**
     * DELETE /api/invoices/{invoiceId}/details/{detailId} - Xóa dòng chi phí
     */
    @DeleteMapping("/invoices/{invoiceId}/details/{detailId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteInvoiceDetail(
            @PathVariable Long invoiceId,
            @PathVariable Long detailId) {
        invoiceService.deleteInvoiceDetail(invoiceId, detailId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data("Invoice detail deleted successfully")
                .build());
    }
}
