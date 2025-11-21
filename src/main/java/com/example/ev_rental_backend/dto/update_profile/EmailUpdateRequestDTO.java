package com.example.ev_rental_backend.dto.update_profile;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailUpdateRequestDTO {

    @NotBlank(message = "Email mới không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
}
