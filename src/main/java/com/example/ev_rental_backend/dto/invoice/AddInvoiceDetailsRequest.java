package com.example.ev_rental_backend.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddInvoiceDetailsRequest {
    private List<Long> priceListIds; // danh sách phụ tùng chọn từ UI
}
