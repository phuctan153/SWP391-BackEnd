package com.example.ev_rental_backend.dto.invoice;

import com.example.ev_rental_backend.entity.InvoiceDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetailResponseDto {

    private Long invoiceDetailId;
    private InvoiceDetail.LineType type;
    private Long priceListId;
    private String itemName;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Double lineTotal;
}
