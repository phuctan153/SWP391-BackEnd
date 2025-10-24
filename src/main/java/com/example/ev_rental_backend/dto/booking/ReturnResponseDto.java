package com.example.ev_rental_backend.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResponseDto {

    private Long bookingId;
    private LocalDateTime actualReturnTime;
    private Double lateFee;
    private Double damageFee;
    private Double totalFee;
    private String message;
}
