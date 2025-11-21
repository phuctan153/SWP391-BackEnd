package com.example.ev_rental_backend.dto.payos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayOSCreatePaymentRequest {
    @JsonProperty("orderCode")
    private Long orderCode; // Unique order code (timestamp)

    @JsonProperty("amount")
    private Integer amount; // Số tiền (VND)

    @JsonProperty("description")
    private String description; // Mô tả đơn hàng

    @JsonProperty("buyerName")
    private String buyerName; // Tên người mua (optional)

    @JsonProperty("buyerEmail")
    private String buyerEmail; // Email người mua (optional)

    @JsonProperty("buyerPhone")
    private String buyerPhone; // SĐT người mua (optional)

    @JsonProperty("buyerAddress")
    private String buyerAddress; // Địa chỉ (optional)

    @JsonProperty("items")
    private List<PayOSItem> items; // Danh sách sản phẩm

    @JsonProperty("cancelUrl")
    private String cancelUrl; // URL khi hủy

    @JsonProperty("returnUrl")
    private String returnUrl; // URL sau khi thanh toán

    @JsonProperty("expiredAt")
    private Long expiredAt; // Thời gian hết hạn (timestamp)

    @JsonProperty("signature")
    private String signature; // Chữ ký HMAC SHA256
}
