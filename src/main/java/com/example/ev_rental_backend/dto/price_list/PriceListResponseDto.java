package com.example.ev_rental_backend.dto.price_list;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceListResponseDto {
    private Long priceId;
    private String itemName;
    private String description;
    private Double unitPrice;
    private Integer stockQuantity;
    private String sparePartType;
}
