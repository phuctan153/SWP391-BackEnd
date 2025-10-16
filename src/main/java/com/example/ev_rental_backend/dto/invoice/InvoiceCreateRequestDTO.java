package com.example.ev_rental_backend.dto.invoice;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCreateRequestDTO {
    @NotNull(message = "Booking ID không được để trống")
    @Positive(message = "Booking ID phải là số dương")
    private Long bookingId;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes;

    // Danh sách các line items (spare parts, penalties, services)
    private List<InvoiceLineItemDTO> lineItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceLineItemDTO {

        @NotBlank(message = "Loại dòng không được để trống")
        @Pattern(regexp = "^(SPAREPART|SERVICE|PENALTY)$",
                message = "Loại phải là: SPAREPART, SERVICE, PENALTY")
        private String type;

        private Long sparepartId; // Nếu type = SPAREPART

        @NotBlank(message = "Mô tả không được để trống")
        private String description;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải >= 1")
        private Integer quantity;

        @NotNull(message = "Đơn giá không được để trống")
        @DecimalMin(value = "0.0", message = "Đơn giá phải >= 0")
        private Double unitPrice;
    }
}
