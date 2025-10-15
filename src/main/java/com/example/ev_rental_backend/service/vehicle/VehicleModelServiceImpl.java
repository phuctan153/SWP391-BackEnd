package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelRequestDTO;
import com.example.ev_rental_backend.dto.vehicle_model.VehicleModelResponseDTO;
import com.example.ev_rental_backend.entity.VehicleModel;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.mapper.VehicleModelMapper;
import com.example.ev_rental_backend.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleModelMapper vehicleModelMapper;

    @Override
    @Transactional
    public VehicleModelResponseDTO createVehicleModel(VehicleModelRequestDTO requestDTO) {
        // 1️⃣ Kiểm tra tên model đã tồn tại chưa
        if (vehicleModelRepository.existsByModelNameIgnoreCase(requestDTO.getModelName())) {
            throw new CustomException("Model '" + requestDTO.getModelName() + "' đã tồn tại trong hệ thống");
        }

        // 2️⃣ Validate dữ liệu cơ bản
        if (requestDTO.getBatteryCapacity() == null || requestDTO.getBatteryCapacity() <= 0) {
            throw new CustomException("Dung lượng pin phải lớn hơn 0");
        }
        if (requestDTO.getSeatingCapacity() <= 0) {
            throw new CustomException("Số ghế phải lớn hơn 0");
        }

        // 3️⃣ Map DTO → Entity
        VehicleModel model = vehicleModelMapper.toEntity(requestDTO);

        // 4️⃣ Lưu vào DB
        VehicleModel savedModel = vehicleModelRepository.save(model);

        // 5️⃣ Map Entity → Response DTO
        return vehicleModelMapper.toResponseDto(savedModel);
    }
}
