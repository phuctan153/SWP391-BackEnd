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

    @Mapping(target = "vehicleId", ignore = true)
    @Mapping(target = "station", ignore = true)
    @Mapping(target = "model", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Vehicle toEntity(VehicleRequestDTO dto);

    @Mapping(target = "status", expression = "java(vehicle.getStatus() != null ? vehicle.getStatus().toString() : null)")
    @Mapping(target = "station", source = "station")
    @Mapping(target = "model", source = "model")
    VehicleResDTO toDto(Vehicle vehicle);

    @Mapping(target = "carNumber", source = "car_number")
    @Mapping(target = "status", expression = "java(station.getStatus() != null ? station.getStatus().toString() : null)")
    VehicleResDTO.StationBasicDTO toStationBasicDto(Station station);

    @Mapping(target = "modelId", source = "modelId")
    @Mapping(target = "modelName", source = "modelName")
    @Mapping(target = "manufacturer", source = "manufacturer")
    @Mapping(target = "batteryCapacity", source = "batteryCapacity")
    @Mapping(target = "seatingCapacity", source = "seatingCapacity")
    VehicleResDTO.VehicleModelBasicDTO toVehicleModelBasicDto(VehicleModel model);

    /**
     * Custom mapping: String -> Vehicle.Status enum
     */
    @Named("stringToStatus")
    default Vehicle.Status stringToStatus(String status) {
        if (status == null || status.isBlank()) {
            return Vehicle.Status.AVAILABLE;
        }
        try {
            return Vehicle.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Vehicle.Status.AVAILABLE;
        }
    }
}
