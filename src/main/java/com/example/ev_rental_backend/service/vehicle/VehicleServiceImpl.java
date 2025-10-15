package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleStatusResponse;
import com.example.ev_rental_backend.dto.vehicle.VehicleStatusUpdate;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private StationRepository stationRepository;

    @Override
    public List<VehicleResponseDTO> getVehiclesByStationId(Long stationId) {
        List<Vehicle> vehicles = vehicleRepository.findByStationId(stationId);
        return vehicleMapper.toResponseDtoList(vehicles);
    }

    /**
     * Tạo mới xe gắn với trạm và model
     */
    @Override
    @Transactional
    public VehicleResDTO createVehicle(VehicleRequestDTO requestDTO) {
        // 1. Validate và lấy Station
        Station station = stationRepository.findById(requestDTO.getStationId())
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy trạm với ID: " + requestDTO.getStationId()
                ));

        // Kiểm tra trạm có đang hoạt động không
        if (station.getStatus() != Station.Status.ACTIVE) {
            throw new CustomException(
                    "Trạm " + station.getName() + " hiện không hoạt động. Không thể thêm xe mới."
            );
        }

        // 2. Validate và lấy VehicleModel
        VehicleModel vehicleModel = vehicleModelRepository.findById(requestDTO.getModelId())
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy model xe với ID: " + requestDTO.getModelId()
                ));

        // 3. Kiểm tra biển số xe đã tồn tại chưa
        if (vehicleRepository.existsByPlateNumber(requestDTO.getPlateNumber())) {
            throw new CustomException(
                    "Biển số xe " + requestDTO.getPlateNumber() + " đã tồn tại trong hệ thống"
            );
        }

        // 4. Kiểm tra số lượng xe trong trạm (car_number)
        long currentVehicleCount = Optional.ofNullable(station.getVehicles())
                .map(vehicles -> vehicles.stream()
                        .filter(v -> v.getStatus() != Vehicle.Status.MAINTENANCE)
                        .count())
                .orElse(0L);

        if (currentVehicleCount >= station.getCar_number()) {
            throw new CustomException(
                    "Trạm " + station.getName() + " đã đầy (" +
                            currentVehicleCount + "/" + station.getCar_number() + "). " +
                            "Không thể thêm xe mới."
            );
        }

        // 5. Validate dữ liệu đầu vào
        validateVehicleData(requestDTO);

        // 6. Map DTO -> Entity
        Vehicle vehicle = vehicleMapper.toEntity(requestDTO);

        // 7. Gán station và model vào vehicle
        vehicle.setStation(station);
        vehicle.setModel(vehicleModel);

        // 8. Lưu vehicle vào database
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        // 9. Fetch lại với station và model để tránh lazy loading
        Vehicle vehicleWithRelations = vehicleRepository
                .findByIdWithStationAndModel(savedVehicle.getVehicleId())
                .orElse(savedVehicle);

        // 10. Map Entity -> Response DTO và trả về
        return vehicleMapper.toDto(vehicleWithRelations);
    }

    /**
     * Validate dữ liệu đầu vào
     */
    private void validateVehicleData(VehicleRequestDTO requestDTO) {
        // Kiểm tra mức pin hợp lý cho xe mới
        if (requestDTO.getBatteryLevel() < 20.0) {
            throw new CustomException(
                    "Xe mới tạo phải có mức pin >= 20%"
            );
        }

        // Kiểm tra quãng đường hợp lý
        if (requestDTO.getMileage() > 100000.0) {
            throw new CustomException(
                    "Quãng đường vượt quá giới hạn cho phép (100,000 km)"
            );
        }

        // Kiểm tra giá thuê theo ngày phải lớn hơn giá thuê theo giờ * 8
        if (requestDTO.getPricePerDay() < requestDTO.getPricePerHour() * 8) {
            throw new CustomException(
                    "Giá thuê theo ngày phải >= giá thuê theo giờ x 8"
            );
        }

        // Kiểm tra giá thuê hợp lý
        if (requestDTO.getPricePerHour() < 10000.0) {
            throw new CustomException(
                    "Giá thuê theo giờ phải >= 10,000 VNĐ"
            );
        }

        if (requestDTO.getPricePerDay() < 50000.0) {
            throw new CustomException(
                    "Giá thuê theo ngày phải >= 50,000 VNĐ"
            );
        }
    }

    @Override
    @Transactional
    public VehicleStatusResponse updateVehicleStatus(Long vehicleId, VehicleStatusUpdate requestDTO) {
        // 1. Lấy thông tin xe
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy xe với ID: " + vehicleId
                ));

        // 2. Parse trạng thái mới
        Vehicle.Status newStatus;
        try {
            newStatus = Vehicle.Status.valueOf(requestDTO.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(
                    "Trạng thái không hợp lệ: " + requestDTO.getStatus()
            );
        }

        // 3. Kiểm tra trạng thái hiện tại
        Vehicle.Status previousStatus = vehicle.getStatus();

        if (previousStatus == newStatus) {
            throw new CustomException(
                    "Xe đang ở trạng thái " + newStatus + ". Không cần cập nhật."
            );
        }

        // 5. Cập nhật trạng thái
        vehicle.setStatus(newStatus);
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);

        // 6. Tạo response
        return VehicleStatusResponse.builder()
                .vehicleId(updatedVehicle.getVehicleId())
                .vehicleName(updatedVehicle.getVehicleName())
                .plateNumber(updatedVehicle.getPlateNumber())
                .previousStatus(previousStatus.toString())
                .currentStatus(newStatus.toString())
                .updatedAt(LocalDateTime.now())
                .updatedBy("SYSTEM") // TODO: Lấy từ JWT token
                .build();
    }
}
