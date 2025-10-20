package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.payment.*;
import com.example.ev_rental_backend.entity.Invoice;

public interface PaymentService {

    public PaymentResponseDto payByCash(Long invoiceId, PaymentRequestDto requestDto);
    /**
     * Thanh toán bằng ví (BR-30)
     */
    public PaymentResponseDto payByWallet(Long invoiceId, PaymentRequestDto requestDto);

    /**
     * Thanh toán qua MoMo (BR-30)
     */
    public MomoPaymentResponseDto payByMomo(Long invoiceId, PaymentRequestDto requestDto);
    /**
     * Thử lại giao dịch thất bại (BR-29)
     */
    public PaymentResponseDto retryPayment(Long invoiceId, RetryPaymentRequestDto requestDto);

    /**
     * Lấy chi tiết giao dịch
     */
    public TransactionResponseDto getTransactionById(Long transactionId);

    /**
     * Xử lý callback từ MoMo
     */
    public void handleMomoCallback(MomoCallbackDto callbackDto);
}
