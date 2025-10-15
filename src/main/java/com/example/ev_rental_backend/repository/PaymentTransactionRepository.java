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

    /**
     * Tìm transaction theo invoice
     */
    List<PaymentTransaction> findByInvoice_InvoiceId(Long invoiceId);

    /**
     * Tìm transaction thành công gần nhất của invoice
     */
    @Query("SELECT pt FROM PaymentTransaction pt " +
            "WHERE pt.invoice.invoiceId = :invoiceId " +
            "AND pt.status = 'SUCCESS' " +
            "ORDER BY pt.transactionTime DESC")
    Optional<PaymentTransaction> findLatestSuccessTransaction(@Param("invoiceId") Long invoiceId);

    /**
     * Kiểm tra invoice đã có transaction thành công chưa
     */
    boolean existsByInvoice_InvoiceIdAndStatus(Long invoiceId, PaymentTransaction.Status status);
}
