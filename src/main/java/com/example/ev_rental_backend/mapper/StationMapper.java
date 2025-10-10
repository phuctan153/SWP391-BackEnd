package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StationMapper {

    @Mapping(target = "vehicles", source = "vehicles")
    StationResponseDTO toDto(Station station);

    List<StationResponseDTO> toDtoList(List<Station> stations);

    VehicleResponseDTO toVehicleDto(Vehicle vehicle);
}
