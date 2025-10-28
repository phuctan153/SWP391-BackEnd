package com.example.ev_rental_backend.service.invoice;

import com.example.ev_rental_backend.dto.invoice.*;

import java.util.List;

public interface InvoiceService {
    public InvoiceResponseDto getInvoiceById(Long invoiceId);
    public List<InvoiceResponseDto> getInvoicesByBookingId(Long bookingId);
    public InvoiceResponseDto createDepositInvoice(Long bookingId, CreateDepositInvoiceDto requestDto);
    public InvoiceResponseDto createFinalInvoice(Long bookingId, CreateFinalInvoiceDto requestDto);
    public InvoiceDetailResponseDto addInvoiceDetail(Long invoiceId, CreateInvoiceDetailDto requestDto);
    public void deleteInvoiceDetail(Long invoiceId, Long detailId);
}
