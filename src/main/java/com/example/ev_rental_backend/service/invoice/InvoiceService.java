package com.example.ev_rental_backend.service.invoice;

import com.example.ev_rental_backend.dto.invoice.*;

import java.util.List;
import java.util.Map;

public interface InvoiceService {
    public InvoiceResponseDto getInvoiceById(Long invoiceId);
    public List<InvoiceResponseDto> getInvoicesByBookingId(Long bookingId);
    public InvoiceResponseDto createDepositInvoice(Long bookingId);
    public InvoiceResponseDto createFinalInvoice(Long bookingId);
    public void deleteInvoiceDetail(Long invoiceId, Long detailId);

    public InvoiceResponseDto markInvoiceAsPaid(Long invoiceId);

    public InvoiceDetailResponseDto addInvoiceDetail(Long invoiceId, CreateInvoiceDetailDto requestDto);
    public InvoiceDetailResponseDto updateInvoiceDetail(Long invoiceId, Long detailId,
                                                        UpdateInvoiceDetailDto requestDto);
    public void recalculateInvoiceTotalAmount(Long invoiceId);
    public Map<String, Object> getInvoiceAmountBreakdown(Long invoiceId);
}
