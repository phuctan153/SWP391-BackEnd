package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.entity.VehicleModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "modelName", source = "model.modelName") // nếu Vehicle có quan hệ với Model
    VehicleResponseDTO toResponseDto(Vehicle vehicle);

    List<VehicleResponseDTO> toResponseDtoList(List<Vehicle> vehicles);

    @Mapping(source = "station.name", target = "stationName")
    VehicleResDTO toResponseDTO(Vehicle vehicle);
}
