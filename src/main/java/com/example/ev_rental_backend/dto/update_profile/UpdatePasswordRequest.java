package com.example.ev_rental_backend.dto.update_profile;

import com.example.ev_rental_backend.dto.renter.validation.PasswordMatches;
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
public class UpdatePasswordRequest {
    private String oldPassword;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu cần có ít nhất 8 ký tự, 1 chữ hoa, 1 số và 1 ký tự đặc biệt"
    )
    private String newPassword;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu")
    private String confirmPassword;
}
