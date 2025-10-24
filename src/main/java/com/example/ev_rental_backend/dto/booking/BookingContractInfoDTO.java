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
public class BookingContractInfoDTO {
    private Long bookingId;

    // Thông tin xe
    private String vehicleName;
    private String vehiclePlate;

    // Thông tin renter (từ giấy tờ xác minh)
    private String renterName;
    private String renterEmail;
    private String renterPhone;

    // Thông tin nhân viên tạo hợp đồng
    private String staffName;

    // Thông tin thuê xe
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Double pricePerHour;
    private Double pricePerDay;
    private String bookingStatus;
}
