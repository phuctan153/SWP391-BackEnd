package com.example.ev_rental_backend.dto.wallet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundByBookingRequestDTO {
    private Long bookingId;

    @Min(value = 0, message = "Phần trăm hoàn tiền phải >= 0")
    @Max(value = 100, message = "Phần trăm hoàn tiền phải <= 100")
    private double refundPercent;
}
