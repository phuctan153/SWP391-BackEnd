package com.example.ev_rental_backend.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineResponseDTO {
    private Long invoiceLineId;
    private String type;
    private String description;
    private int quantity;
    private Double unitPrice;
    private Double lineTotal;
}