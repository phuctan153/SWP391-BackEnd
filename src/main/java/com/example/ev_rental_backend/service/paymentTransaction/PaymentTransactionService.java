package com.example.ev_rental_backend.service.paymentTransaction;

import com.example.ev_rental_backend.entity.PaymentTransaction;

import java.util.List;

public interface PaymentTransactionService {
    public List<PaymentTransaction> getAll();
    public PaymentTransaction getById(Long id);
    public List<PaymentTransaction> getByInvoice(Long invoiceId);
}
