package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.IdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdentityDocumentRepository extends JpaRepository<IdentityDocument, Long> {

    // 🔹 Tìm giấy tờ theo renter
    List<IdentityDocument> findByRenter_RenterId(Long renterId);

    // 🔹 Tìm giấy tờ theo loại (CCCD, GPLX, Passport)
    List<IdentityDocument> findByRenter_RenterIdAndType(Long renterId, IdentityDocument.DocumentType type);

    // 🔹 Tìm giấy tờ cụ thể theo số và loại
    Optional<IdentityDocument> findByDocumentNumberAndType(String documentNumber, IdentityDocument.DocumentType type);

    // 🔹 Tìm các giấy tờ đang chờ duyệt
    List<IdentityDocument> findByStatus(IdentityDocument.DocumentStatus status);
}
