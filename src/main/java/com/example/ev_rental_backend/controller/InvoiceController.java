package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.invoice.*;
import com.example.ev_rental_backend.entity.PriceList;
import com.example.ev_rental_backend.service.invoice.InvoiceService;
import com.example.ev_rental_backend.service.price_list.PriceListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PriceListService priceListService;

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
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> createDepositInvoice(
            @PathVariable Long bookingId) {
        InvoiceResponseDto invoice = invoiceService.createDepositInvoice(bookingId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(invoice)
                        .message("Tạo hóa đơn tiền cọc thành công")
                        .build());
    }


    /**
     * POST /api/bookings/{bookingId}/invoices/final - Tạo hóa đơn cuối (BR-27)
     */
    @PostMapping("/bookings/{bookingId}/invoices/final")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> createFinalInvoice(
            @PathVariable Long bookingId) {
        InvoiceResponseDto invoice = invoiceService.createFinalInvoice(bookingId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .data(invoice)
                        .build());
    }




    // 7.2. Invoice Details
    /**
     * POST /api/v1/invoices/{invoiceId}/details - Thêm dòng chi phí (phụ tùng, phạt) (BR-13)
     */
    @PostMapping("/invoices/{invoiceId}/details")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceDetailResponseDto>> addInvoiceDetail(
            @PathVariable Long invoiceId,
            @Valid @RequestBody CreateInvoiceDetailDto requestDto) {

        log.info("Adding invoice detail to invoice {}: type={}, quantity={}, unitPrice={}",
                invoiceId, requestDto.getType(), requestDto.getQuantity(), requestDto.getUnitPrice());

        InvoiceDetailResponseDto detail = invoiceService.addInvoiceDetail(invoiceId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<InvoiceDetailResponseDto>builder()
                        .status("success")
                        .code(HttpStatus.CREATED.value())
                        .message("Invoice detail added successfully")
                        .data(detail)
                        .build());
    }

    /**
     * PUT /api/v1/invoices/{invoiceId}/details/{detailId} - Cập nhật dòng chi phí
     */
    @PutMapping("/invoices/{invoiceId}/details/{detailId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceDetailResponseDto>> updateInvoiceDetail(
            @PathVariable Long invoiceId,
            @PathVariable Long detailId,
            @Valid @RequestBody UpdateInvoiceDetailDto requestDto) {

        log.info("Updating invoice detail {} of invoice {}", detailId, invoiceId);

        InvoiceDetailResponseDto detail = invoiceService.updateInvoiceDetail(invoiceId, detailId, requestDto);

        return ResponseEntity.ok(ApiResponse.<InvoiceDetailResponseDto>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Invoice detail updated successfully")
                .data(detail)
                .build());
    }

    /**
     * DELETE /api/v1/invoices/{invoiceId}/details/{detailId} - Xóa dòng chi phí
     */
    @DeleteMapping("/invoices/{invoiceId}/details/{detailId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoiceDetail(
            @PathVariable Long invoiceId,
            @PathVariable Long detailId) {

        log.info("Deleting invoice detail {} from invoice {}", detailId, invoiceId);

        invoiceService.deleteInvoiceDetail(invoiceId, detailId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Invoice detail deleted successfully")
                .build());
    }

    @GetMapping("/spare-parts")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PriceList>>> getAllSpareParts() {
        List<PriceList> spareParts = priceListService.getAllSpareParts();

        return ResponseEntity.ok(
                ApiResponse.<List<PriceList>>builder()
                        .status("success")
                        .code(HttpStatus.OK.value())
                        .data(spareParts)
                        .message("Danh sách phụ tùng đã được tải thành công")
                        .build()
        );
    }

    /**
     * GET /api/v1/invoices/{invoiceId}/amount-breakdown - Xem phân tích chi tiết tổng tiền
     */
    @GetMapping("/invoices/{invoiceId}/amount-breakdown")
    @PreAuthorize("hasAnyRole('RENTER', 'STAFF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInvoiceAmountBreakdown(
            @PathVariable Long invoiceId) {

        log.info("Getting amount breakdown for invoice {}", invoiceId);

        Map<String, Object> breakdown = invoiceService.getInvoiceAmountBreakdown(invoiceId);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(breakdown)
                .build());
    }

    /**
     * POST /api/v1/invoices/{invoiceId}/recalculate - Tính lại tổng tiền invoice (Admin only)
     */
    @PostMapping("/invoices/{invoiceId}/recalculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> recalculateInvoiceTotalAmount(
            @PathVariable Long invoiceId) {

        log.info("Recalculating total amount for invoice {}", invoiceId);

        invoiceService.recalculateInvoiceTotalAmount(invoiceId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .message("Invoice total amount recalculated successfully")
                .build());
    }
}
