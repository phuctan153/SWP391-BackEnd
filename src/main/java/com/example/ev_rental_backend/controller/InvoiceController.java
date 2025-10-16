package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.invoice.InvoiceCreateRequestDTO;
import com.example.ev_rental_backend.dto.invoice.InvoiceResponseDTO;
import com.example.ev_rental_backend.service.Invoice.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * API lấy chi tiết invoice
     * GET /api/invoices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoiceById(
            @PathVariable Long id) {

        InvoiceResponseDTO invoiceData = invoiceService.getInvoiceById(id);

        ApiResponse<InvoiceResponseDTO> response = ApiResponse.<InvoiceResponseDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(invoiceData)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * API lấy invoice của booking
     * GET /api/invoices/booking/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponseDTO>>> getInvoicesByBookingId(
            @PathVariable Long bookingId) {

        List<InvoiceResponseDTO> invoicesData = invoiceService.getInvoicesByBookingId(bookingId);

        ApiResponse<List<InvoiceResponseDTO>> response = ApiResponse.<List<InvoiceResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(invoicesData)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * API tạo invoice Final
     * POST /api/invoices
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> createFinalInvoice(
            @Valid @RequestBody InvoiceCreateRequestDTO requestDTO) {

        InvoiceResponseDTO invoiceData = invoiceService.createFinalInvoice(requestDTO);

        ApiResponse<InvoiceResponseDTO> response = ApiResponse.<InvoiceResponseDTO>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(invoiceData)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
