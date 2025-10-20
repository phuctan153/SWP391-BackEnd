package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByInvoice_InvoiceId(Long invoiceId);

    List<PaymentTransaction> findByWallet_WalletId(Long walletId);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.invoice.invoiceId = :invoiceId " +
            "AND pt.status = :status")
    List<PaymentTransaction> findByInvoiceIdAndStatus(@Param("invoiceId") Long invoiceId,
                                                      @Param("status") PaymentTransaction.Status status);

    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = :status " +
            "AND pt.transactionType IN :types")
    List<PaymentTransaction> findByStatusAndTransactionTypeIn(
            @Param("status") PaymentTransaction.Status status,
            @Param("types") List<PaymentTransaction.TransactionType> types);
}
