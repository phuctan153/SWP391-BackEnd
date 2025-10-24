package com.example.ev_rental_backend.dto.renter.validation;

import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.forgot_password.ResetPasswordWithOtpDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        String password = null;
        String confirmPassword = null;

        if (obj instanceof RenterRequestDTO dto) {
            password = dto.getPassword();
            confirmPassword = dto.getConfirmPassword();
        } else if (obj instanceof ResetPasswordWithOtpDTO dto) {
            password = dto.getPassword();
            confirmPassword = dto.getConfirmPassword();
        }

        if (password == null || confirmPassword == null) {
            return false;
        }

        boolean matches = password.equals(confirmPassword);
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Mật khẩu xác nhận không khớp")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return matches;
    }
}
