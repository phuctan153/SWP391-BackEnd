package com.example.ev_rental_backend.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSItem {
    @JsonProperty("name")
    private String name; // Tên sản phẩm

    @JsonProperty("quantity")
    private Integer quantity; // Số lượng

    @JsonProperty("price")
    private Integer price; // Đơn giá
}
