package com.example.ev_rental_backend.dto.spare_part;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SparePartResponseDTO {
    private Long sparepartId;
    private String partName;
    private String description;
    private Double unitPrice;
    private int stockQuantity;
}

