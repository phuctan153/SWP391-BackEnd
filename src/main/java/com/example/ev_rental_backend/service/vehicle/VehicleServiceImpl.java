package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.entity.VehicleImage;
import com.example.ev_rental_backend.entity.VehicleModel;
import com.example.ev_rental_backend.entity.VehicleModel;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.mapper.VehicleMapper;
import com.example.ev_rental_backend.repository.StationRepository;
import com.example.ev_rental_backend.repository.VehicleModelRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private VehicleMapper vehicleMapper;
    @Override
    public List<VehicleResponseDTO> getVehiclesByStationId(Long stationId) {
        List<Vehicle> vehicles = vehicleRepository.findVehiclesByStationSorted(stationId);
        return vehicleMapper.toResponseDtoList(vehicles);
    }

    @Override
    public VehicleDetailResponseDTO getVehicleDetail(Long vehicleId) {

        Vehicle vehicle = vehicleRepository.findWithBookingsByVehicleId(vehicleId);
        if (vehicle == null) {
            throw new RuntimeException("Không tìm thấy xe có ID: " + vehicleId);
        }

        VehicleDetailResponseDTO dto = vehicleMapper.toVehicleDetailDto(vehicle);
        dto.setBookingHistory(vehicleMapper.toBookingHistoryDtoList(vehicle.getBookings()));

        dto.setFeedbacks(vehicle.getBookings().stream()
                .filter(b -> b.getBookingRating() != null)
                .map(b -> vehicleMapper.toVehicleFeedbackDto(b.getBookingRating()))
                .toList());

        if (vehicle.getImages() != null && !vehicle.getImages().isEmpty()) {
            dto.setImageUrls(
                    vehicle.getImages().stream()
                            .map(VehicleImage::getImageUrl)
                            .toList()
            );
        } else {
            dto.setImageUrls(List.of()); // Trả list rỗng nếu không có ảnh
        }

        return dto;
    }

    @Override
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDTO createVehicle(VehicleDTO dto) {
        if (vehicleRepository.existsByPlateNumber(dto.getPlateNumber())) {
            throw new RuntimeException("Plate number already exists");
        }

        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new RuntimeException("Station not found"));
        VehicleModel model = vehicleModelRepository.findById(dto.getModelId())
                .orElseThrow(() -> new RuntimeException("Model not found"));

        Vehicle v = Vehicle.builder()
                .vehicleName(dto.getVehicleName())
                .station(station)
                .model(model)
                .pricePerHour(dto.getPricePerHour())
                .pricePerDay(dto.getPricePerDay())
                .plateNumber(dto.getPlateNumber())
                .batteryLevel(dto.getBatteryLevel())
                .mileage(dto.getMileage())
                .description(dto.getDescription())
                .status(dto.getStatus() == null ? Vehicle.Status.AVAILABLE : dto.getStatus())
                .build();

        vehicleRepository.save(v);
        return mapToDTO(v);
    }

    @Override
    public VehicleDTO updateVehicle(Long id, VehicleDTO dto) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        v.setVehicleName(dto.getVehicleName());
        v.setPricePerHour(dto.getPricePerHour());
        v.setPricePerDay(dto.getPricePerDay());
        v.setBatteryLevel(dto.getBatteryLevel());
        v.setMileage(dto.getMileage());
        v.setDescription(dto.getDescription());
        v.setStatus(dto.getStatus());

        // Cập nhật quan hệ nếu có thay đổi
        if (dto.getStationId() != null) {
            Station station = stationRepository.findById(dto.getStationId())
                    .orElseThrow(() -> new RuntimeException("Station not found"));
            v.setStation(station);
        }

        if (dto.getModelId() != null) {
            VehicleModel model = vehicleModelRepository.findById(dto.getModelId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));
            v.setModel(model);
        }

        vehicleRepository.save(v);
        return mapToDTO(v);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicleRepository.delete(v);
    }

    private VehicleDTO mapToDTO(Vehicle v) {
        return VehicleDTO.builder()
                .vehicleId(v.getVehicleId())
                .vehicleName(v.getVehicleName())
                .stationId(v.getStation().getStationId())
                .stationName(v.getStation().getName())
                .modelId(v.getModel().getModelId())
                .modelName(v.getModel().getModelName())
                .pricePerHour(v.getPricePerHour())
                .pricePerDay(v.getPricePerDay())
                .plateNumber(v.getPlateNumber())
                .batteryLevel(v.getBatteryLevel())
                .mileage(v.getMileage())
                .description(v.getDescription())
                .status(v.getStatus())
                .build();
    }
}
