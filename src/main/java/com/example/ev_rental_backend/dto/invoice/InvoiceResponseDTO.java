package com.example.ev_rental_backend.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {
    private Long invoiceId;
    private Long bookingId;
    private LocalDate issueDate;
    private Double totalAmount;
    private String status;
    private List<InvoiceLineResponseDTO> lines;
}
