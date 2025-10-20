package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    List<InvoiceDetail> findByInvoice_InvoiceId(Long invoiceId);

    @Query("SELECT id FROM InvoiceDetail id WHERE id.invoice.invoiceId = :invoiceId AND id.type = :type")
    List<InvoiceDetail> findByInvoiceIdAndType(@Param("invoiceId") Long invoiceId,
                                               @Param("type") InvoiceDetail.LineType type);
}
