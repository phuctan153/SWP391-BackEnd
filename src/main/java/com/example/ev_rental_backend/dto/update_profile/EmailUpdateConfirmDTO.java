package com.example.ev_rental_backend.dto.update_profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailUpdateConfirmDTO {

    @NotNull
    private Long renterId;

    @NotNull
    private String newEmail;

    @NotBlank
    private String otpCode;
}
