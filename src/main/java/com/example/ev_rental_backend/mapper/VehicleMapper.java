package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "modelName", source = "model.modelName") // nếu Vehicle có quan hệ với Model
    VehicleResponseDTO toResponseDto(Vehicle vehicle);

    List<VehicleResponseDTO> toResponseDtoList(List<Vehicle> vehicles);
}
