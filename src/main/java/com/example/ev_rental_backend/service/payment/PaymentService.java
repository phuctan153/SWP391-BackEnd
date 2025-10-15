package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.invoice.InvoiceSummaryDTO;
import com.example.ev_rental_backend.dto.payment.PaymentInitRequestDTO;
import com.example.ev_rental_backend.dto.payment.PaymentResponseDTO;

public interface PaymentService {
    /**
     * Khởi tạo thanh toán cho invoice
     */
    PaymentResponseDTO initPayment(PaymentInitRequestDTO requestDTO);

    /**
     * Lấy thông tin invoice
     */
    InvoiceSummaryDTO getInvoiceSummary(Long invoiceId);

    /**
     * Xử lý callback thành công từ Momo
     */
    void handlePaymentSuccess(Long invoiceId, String transactionId, Double amount);

    /**
     * Xử lý callback thất bại từ Momo
     */
    void handlePaymentFailed(Long invoiceId, String reason);
}
