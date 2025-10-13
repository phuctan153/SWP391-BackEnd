package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Renter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RenterMapper {
    RenterResponseDTO toResponseDto(Renter renter);
    Renter toEntity(RenterRequestDTO dto);
}
