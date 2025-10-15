package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.station_vehicle.CreateStationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StationMapper {

    @Mapping(target = "vehicles", source = "vehicles")
    StationResponseDTO toDto(Station station);

    List<StationResponseDTO> toDtoList(List<Station> stations);

    VehicleResponseDTO toVehicleDto(Vehicle vehicle);


    @Mapping(target = "stationId", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "staffStations", ignore = true)
    @Mapping(target = "car_number", source = "carNumber")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    Station toEntity(StationRequestDTO dto);

    @Mapping(target = "carNumber", source = "car_number")
    @Mapping(target = "status", expression = "java(station.getStatus() != null ? station.getStatus().toString() : null)")
    @Mapping(target = "currentVehicleCount", expression = "java(calculateCurrentVehicleCount(station))")
    @Mapping(target = "availableVehicleCount", expression = "java(calculateAvailableVehicleCount(station))")
    @Mapping(target = "vehicles", expression = "java(mapVehicles(station))")
    CreateStationResponseDTO toResDto(Station station);

    List<CreateStationResponseDTO> toResDtoList(List<Station> stations);

    @Named("stringToStatus")
    default Station.Status stringToStatus(String status) {
        if (status == null || status.isBlank()) {
            return Station.Status.ACTIVE;
        }
        try {
            return Station.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Station.Status.ACTIVE;
        }
    }

    // Tính toán số xe hiện tại
    default Integer calculateCurrentVehicleCount(Station station) {
        if (station.getVehicles() == null) {
            return 0;
        }
        return (int) station.getVehicles().stream()
                .filter(v -> v.getStatus() != Vehicle.Status.MAINTENANCE)
                .count();
    }

    // Tính toán số xe sẵn có
    default Integer calculateAvailableVehicleCount(Station station) {
        if (station.getVehicles() == null) {
            return 0;
        }
        return (int) station.getVehicles().stream()
                .filter(v -> v.getStatus() == Vehicle.Status.AVAILABLE)
                .count();
    }

    // Map danh sách xe trong trạm
    default List<CreateStationResponseDTO.VehicleBasicDTO> mapVehicles(Station station) {
        if (station.getVehicles() == null) {
            return null;
        }
        return station.getVehicles().stream()
                .map(v -> CreateStationResponseDTO.VehicleBasicDTO.builder()
                        .vehicleId(v.getVehicleId())
                        .vehicleName(v.getVehicleName())
                        .plateNumber(v.getPlateNumber())
                        .batteryLevel(v.getBatteryLevel())
                        .status(v.getStatus() != null ? v.getStatus().toString() : null)
                        .build())
                .collect(Collectors.toList());
    }
}
