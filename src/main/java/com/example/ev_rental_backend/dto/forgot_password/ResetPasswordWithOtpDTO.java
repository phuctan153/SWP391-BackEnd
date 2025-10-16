package com.example.ev_rental_backend.dto.forgot_password;

import com.example.ev_rental_backend.dto.renter.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PasswordMatches(message = "Mật khẩu xác nhận không khớp")
public class ResetPasswordWithOtpDTO {

    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải gồm đúng 6 chữ số")
    private String otpCode;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu cần có ít nhất 8 ký tự, 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
    )
    private String password;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
    private String confirmPassword;
}
