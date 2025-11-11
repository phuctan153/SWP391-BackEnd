package com.example.ev_rental_backend.service.paymentTransaction;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentTransactionServiceImpl implements PaymentTransactionService{

    private final PaymentTransactionRepository paymentTransactionRepository;

    public List<PaymentTransaction> getAll() {
        return paymentTransactionRepository.findAll();
    }

    public List<PaymentTransaction> getByInvoice(Long invoiceId) {
        return paymentTransactionRepository.findByInvoice_InvoiceId(invoiceId);
    }

    public PaymentTransaction getById(Long id) {
        return paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
}
