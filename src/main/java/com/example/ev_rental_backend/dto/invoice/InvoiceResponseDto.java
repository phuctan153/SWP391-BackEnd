package com.example.ev_rental_backend.dto.invoice;

import com.example.ev_rental_backend.entity.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDto {

    private Long invoiceId;
    private Long bookingId;
    private Invoice.Type type;
    private Double depositAmount;
    private Double totalAmount;
    private Double amountRemaining;
    private Invoice.Status status;
    private Invoice.PaymentMethod paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private List<InvoiceDetailResponseDto> details;
}
