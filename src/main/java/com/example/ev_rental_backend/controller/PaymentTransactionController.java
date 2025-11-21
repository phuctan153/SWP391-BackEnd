package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.service.paymentTransaction.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-transactions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    /**
     * GET tất cả hoặc lọc theo invoiceId / walletId
     */
    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> getAll(
            @RequestParam(required = false) Long invoiceId ) {

        if (invoiceId != null) {
            return ResponseEntity.ok(paymentTransactionService.getByInvoice(invoiceId));
        }

        return ResponseEntity.ok(paymentTransactionService.getAll());
    }

    /**
     * GET chi tiết transaction theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentTransaction> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentTransactionService.getById(id));
    }
}
