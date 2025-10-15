package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {
    List<InvoiceDetail> findByInvoice_InvoiceId(Long invoiceId);
}
