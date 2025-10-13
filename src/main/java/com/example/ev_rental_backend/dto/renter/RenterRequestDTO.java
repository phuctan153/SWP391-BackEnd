package com.example.ev_rental_backend.dto.renter;

import com.example.ev_rental_backend.dto.renter.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PasswordMatches(message = "Mật khẩu xác nhận không khớp")
public class RenterRequestDTO {
    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu cần có ít nhất 8 ký tự, 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
    private String confirmPassword;

    @NotBlank(message = "Số điện thoại không hợp lý")
    @Pattern(
            regexp = "^[0-9]{10,}$",
            message = "Số điện thoại phải chứa ít nhất 10 chữ số và chỉ bao gồm ký tự số"
    )
    private String phoneNumber;

    private String nationalId;
    private String driverLicense;
    private LocalDate dateOfBirth;
    private String address;
}
