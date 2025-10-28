package com.example.ev_rental_backend.service.station;

import com.example.ev_rental_backend.dto.station_vehicle.CreateStationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationResponseDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.mapper.StationMapper;
import com.example.ev_rental_backend.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // Tạo mới trạm
    @Override
    @Transactional
    public CreateStationResponseDTO createStation(StationRequestDTO requestDTO) {
        // 1. Kiểm tra tên trạm đã tồn tại chưa
        if (stationRepository.existsByNameIgnoreCase(requestDTO.getName())) {
            throw new CustomException(
                    "Tên trạm '" + requestDTO.getName() + "' đã tồn tại trong hệ thống"
            );
        }

        // 2. Kiểm tra tọa độ có trùng với trạm khác không (trong bán kính ~100m)
        List<Station> nearbyStations = stationRepository.findByNearbyCoordinates(
                requestDTO.getLatitude(),
                requestDTO.getLongitude()
        );

        if (!nearbyStations.isEmpty()) {
            Station existingStation = nearbyStations.get(0);
            throw new CustomException(
                    "Tọa độ này quá gần với trạm '" + existingStation.getName() +
                            "' (khoảng cách < 100m). Vui lòng chọn vị trí khác."
            );
        }

        // 3. Validate dữ liệu đầu vào
        validateStationData(requestDTO);

        // 4. Map DTO -> Entity
        Station station = stationMapper.toEntity(requestDTO);

        // 5. Lưu station vào database
        Station savedStation = stationRepository.save(station);

        // 6. Map Entity -> Response DTO và trả về
        return stationMapper.toResDto(savedStation);
    }

    @Override
    public StationResponseDTO updateStation(Long stationId, StationRequestDTO requestDTO) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạm"));

        station.setName(requestDTO.getName());
        station.setLocation(requestDTO.getLocation());
        station.setLatitude(requestDTO.getLatitude());
        station.setLongitude(requestDTO.getLongitude());
        station.setCar_number(requestDTO.getCarNumber());

        // ✅ Chuyển đổi String → Enum (nếu người dùng có gửi status)
        if (requestDTO.getStatus() != null) {
            try {
                station.setStatus(Station.Status.valueOf(requestDTO.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ! (Chỉ chấp nhận: ACTIVE, INACTIVE)");
            }
        }

        Station updated = stationRepository.save(station);
        return stationMapper.toDto(updated);
    }

    @Override
    public void deleteStation(Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạm"));

        // 🟠 Xóa mềm: chỉ đổi trạng thái
        station.setStatus(Station.Status.INACTIVE);
        stationRepository.save(station);
    }

    /**
     * Validate dữ liệu đầu vào
     */
    private void validateStationData(StationRequestDTO requestDTO) {
        // Kiểm tra tọa độ hợp lý cho Việt Nam
        // Việt Nam: Latitude: 8.5 - 23.4, Longitude: 102.1 - 109.5
        if (requestDTO.getLatitude() < 8.0 || requestDTO.getLatitude() > 24.0) {
            throw new CustomException(
                    "Vĩ độ không hợp lệ cho Việt Nam. Vĩ độ phải trong khoảng 8.0 - 24.0"
            );
        }

        if (requestDTO.getLongitude() < 102.0 || requestDTO.getLongitude() > 110.0) {
            throw new CustomException(
                    "Kinh độ không hợp lệ cho Việt Nam. Kinh độ phải trong khoảng 102.0 - 110.0"
            );
        }

        // Kiểm tra số lượng xe hợp lý
        if (requestDTO.getCarNumber() < 5) {
            throw new CustomException(
                    "Trạm phải có ít nhất 5 vị trí để đảm bảo hiệu quả vận hành"
            );
        }

        if (requestDTO.getCarNumber() > 200) {
            throw new CustomException(
                    "Số lượng xe tối đa không nên vượt quá 200 để đảm bảo quản lý hiệu quả"
            );
        }
    }
}
