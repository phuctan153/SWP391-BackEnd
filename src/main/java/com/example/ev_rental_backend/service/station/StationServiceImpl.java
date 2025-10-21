package com.example.ev_rental_backend.service.station;

import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.mapper.StationMapper;
import com.example.ev_rental_backend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StationServiceImpl implements StationService {

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private StationMapper stationMapper;

    @Override
    public List<StationResponseDTO> getStationsByLocation(double userLat, double userLng) {
        List<Station> stations = stationRepository.findAllStationsWithVehicles();

        return stations.stream()
                .map(station -> {
                    List<Vehicle> vehicles = Optional.ofNullable(station.getVehicles()).orElse(List.of());

                    List<Vehicle> availableVehicles = vehicles.stream()
                            .filter(v -> "AVAILABLE".equalsIgnoreCase(v.getStatus().toString()))
                            .toList();

                    double distance = calculateDistance(
                            userLat, userLng,
                            station.getLatitude(), station.getLongitude()
                    );

                    StationResponseDTO dto = stationMapper.toDto(station);
                    dto.setDistance(distance);
                    dto.setAvailableCount(availableVehicles.size());

                    dto.setVehicles(vehicles.stream()
                            .map(v -> VehicleResponseDTO.builder()
                                    .vehicleId(v.getVehicleId())
                                    .plateNumber(v.getPlateNumber())
                                    .batteryLevel(v.getBatteryLevel())
                                    .status(v.getStatus().toString())
                                    .mileage(v.getMileage())
                                    .build())
                            .collect(Collectors.toList()));

                    return dto;
                })
                // 🔹 Sắp xếp: distance ↑ → availableCount ↓
                .sorted(Comparator
                        .comparingDouble(StationResponseDTO::getDistance)
                        .thenComparing(
                                Comparator.<StationResponseDTO>comparingInt(
                                        s -> s.getAvailableCount() != null ? s.getAvailableCount() : 0
                                ).reversed()
                        )
                )

                .collect(Collectors.toList());
    }

    @Override
    public List<StationResponseDTO> getAllStations() {
        List<Station> stations = stationRepository.findAllStationsWithVehicles();

        return stations.stream()
                .map(station -> {
                    List<Vehicle> vehicles = Optional.ofNullable(station.getVehicles()).orElse(List.of());

                    long availableCount = vehicles.stream()
                            .filter(v -> "AVAILABLE".equalsIgnoreCase(v.getStatus().toString()))
                            .count();

                    StationResponseDTO dto = stationMapper.toDto(station);
                    dto.setAvailableCount((int) availableCount);
                    dto.setDistance(null); // không tính nếu không có vị trí người dùng

                    dto.setVehicles(vehicles.stream()
                            .map(v -> VehicleResponseDTO.builder()
                                    .vehicleId(v.getVehicleId())
                                    .plateNumber(v.getPlateNumber())
                                    .modelName(v.getModel().getModelName())
                                    .batteryLevel(v.getBatteryLevel())
                                    .status(v.getStatus().toString())
                                    .mileage(v.getMileage())
                                    .build())
                            .collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 🧮 Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
