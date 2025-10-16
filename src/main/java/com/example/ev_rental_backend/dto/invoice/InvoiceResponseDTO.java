package com.example.ev_rental_backend.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {

    private Long invoiceId;
    private Long bookingId;

    private String type; // Deposit, Final
    private String status; // PENDING, COMPLETED, CANCELLED, FAILED

    private Double depositAmount;
    private Double totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private String notes;

    // Danh sách line items
    private List<InvoiceLineDetailDTO> lineItems;

    // Thông tin booking cơ bản
    private BookingBasicDTO booking;

    // Thông tin thanh toán
    private List<PaymentTransactionDTO> transactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceLineDetailDTO {
        private Long invoiceDetailId;
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
        private Double unitPrice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingBasicDTO {
        private Long bookingId;
        private String vehicleName;
        private String plateNumber;
        private String renterName;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentTransactionDTO {
        private Long transactionId;
        private Double amount;
        private String status;
        private LocalDateTime transactionTime;
    }
}
