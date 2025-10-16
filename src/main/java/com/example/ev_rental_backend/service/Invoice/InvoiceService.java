package com.example.ev_rental_backend.service.Invoice;

import com.example.ev_rental_backend.dto.invoice.InvoiceCreateRequestDTO;
import com.example.ev_rental_backend.dto.invoice.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {

    /**
     * Lấy chi tiết invoice
     */
    InvoiceResponseDTO getInvoiceById(Long invoiceId);

    /**
     * Lấy invoice của booking
     */
    List<InvoiceResponseDTO> getInvoicesByBookingId(Long bookingId);

    /**
     * Tạo invoice Final (sau khi trả xe)
     */
    InvoiceResponseDTO createFinalInvoice(InvoiceCreateRequestDTO requestDTO);
}
