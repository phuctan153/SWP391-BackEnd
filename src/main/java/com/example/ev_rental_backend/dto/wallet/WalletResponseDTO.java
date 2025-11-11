package com.example.ev_rental_backend.dto.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {
    private Long walletId;
    private Long renterId;
    private String renterName;
    private String renterEmail;
    private BigDecimal balance;
    private String status;
    private String createdAt;
    private String updatedAt;
}
