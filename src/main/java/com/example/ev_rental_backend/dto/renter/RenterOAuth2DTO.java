package com.example.ev_rental_backend.dto.renter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenterOAuth2DTO {
    private String fullName;
    private String email;
    private String googleId;
    private String pictureUrl;
}
