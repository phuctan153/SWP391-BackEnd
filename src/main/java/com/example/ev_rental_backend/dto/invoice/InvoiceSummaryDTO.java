package com.example.ev_rental_backend.dto.invoice;

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
public class InvoiceSummaryDTO {

    private Long invoiceId;
    private Long bookingId;
    private String invoiceType; // Deposit, Final
    private String status;

    private Double depositAmount;
    private Double totalAmount;

    private List<LineItemDTO> lineItems;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemDTO {
        private Long lineId;
        private String type; // SPAREPART, SERVICE, PENALTY
        private String description;
        private Integer quantity;
        private Double unitPrice;
        private Double lineTotal;

        // Nếu là SPAREPART
        private SparePartBasicDTO sparePart;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SparePartBasicDTO {
        private Long sparepartId;
        private String partName;
        private String description;
    }
}
