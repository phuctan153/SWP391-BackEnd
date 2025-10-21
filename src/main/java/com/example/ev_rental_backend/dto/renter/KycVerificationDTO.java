package com.example.ev_rental_backend.dto.renter;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycVerificationDTO {

    // ---------- RENTER ----------
    @NotNull(message = "Renter ID không được để trống")
    private Long renterId;

    // ---------- CCCD ----------
    @NotBlank(message = "Số CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Số CCCD phải gồm đúng 12 chữ số")
    private String nationalId;

    @NotBlank(message = "Tên trên CCCD không được để trống")
    private String nationalName;

    @NotNull(message = "Ngày sinh trên CCCD không được để trống")
    @Past(message = "Ngày sinh phải là trong quá khứ")
    private LocalDate nationalDob;

    @NotBlank(message = "Địa chỉ trên CCCD không được để trống")
    private String nationalAddress;

    @PastOrPresent(message = "Ngày cấp CCCD không được nằm trong tương lai")
    private LocalDate nationalIssueDate;

    @NotNull(message = "Ngày hết hạn CCCD không được để trống")
    @Future(message = "Ngày hết hạn CCCD phải nằm trong tương lai")
    private LocalDate nationalExpireDate;

    // ---------- GPLX ----------
    @NotBlank(message = "Số GPLX không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "Số GPLX phải gồm đúng 12 chữ số")
    private String driverLicense;

    @NotBlank(message = "Tên trên GPLX không được để trống")
    private String driverName;

    @NotBlank(message = "Địa chỉ trên GPLX không được để trống")
    private String driverAddress;

    @NotBlank(message = "Hạng GPLX không được để trống (VD: B1, B2, C...)")
    @Pattern(regexp = "^[A-F][1-3]?$", message = "Hạng GPLX không hợp lệ (VD: B1, B2, C...)")
    private String driverClass;
    
    @PastOrPresent(message = "Ngày cấp GPLX không được nằm trong tương lai")
    private LocalDate driverIssueDate;

    @NotNull(message = "Ngày hết hạn GPLX không được để trống")
    @Future(message = "Ngày hết hạn GPLX phải nằm trong tương lai")
    private LocalDate driverExpireDate;

    // ---------- Optional ----------
    @PositiveOrZero(message = "Điểm tin cậy (confidenceScore) phải ≥ 0")
    private double confidenceScore; // overall_score nếu OCR có trả
}
