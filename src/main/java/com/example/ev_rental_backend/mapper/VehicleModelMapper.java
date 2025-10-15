package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;
import com.example.ev_rental_backend.entity.VehicleModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VehicleModelMapper {
    VehicleModel toEntity(VehicleModelRequestDTO dto);
    VehicleModelResponseDTO toResponseDto(VehicleModel entity);
}
