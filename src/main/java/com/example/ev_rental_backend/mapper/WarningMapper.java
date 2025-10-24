package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.warning.WarningResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarningMapper {

    default WarningResponseDTO toResponseDTO(Booking booking, String message) {
        return WarningResponseDTO.builder()
                .renterEmail(booking.getRenter().getEmail())
                .renterName(booking.getRenter().getFullName())
                .message(message)
                .build();
    }
}
