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

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleModelServiceImpl implements VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;
    private final VehicleModelMapper vehicleModelMapper;

    @Override
    @Transactional
    public VehicleModelResponseDTO createVehicleModel(VehicleModelRequestDTO requestDTO) {
        if (vehicleModelRepository.existsByModelNameIgnoreCase(requestDTO.getModelName())) {
            throw new CustomException("Model '" + requestDTO.getModelName() + "' đã tồn tại trong hệ thống");
        }

        validateModel(requestDTO);

        VehicleModel model = vehicleModelMapper.toEntity(requestDTO);
        VehicleModel saved = vehicleModelRepository.save(model);

        return vehicleModelMapper.toResponseDto(saved);
    }

    @Override
    public List<VehicleModelResponseDTO> getAllVehicleModels() {
        List<VehicleModel> models = vehicleModelRepository.findAll();
        return models.stream()
                .map(vehicleModelMapper::toResponseDto)
                .toList();
    }

    @Override
    public VehicleModelResponseDTO getVehicleModelById(Long id) {
        VehicleModel model = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy model có ID = " + id));
        return vehicleModelMapper.toResponseDto(model);
    }

    @Override
    @Transactional
    public VehicleModelResponseDTO updateVehicleModel(Long id, VehicleModelRequestDTO requestDTO) {
        VehicleModel existing = vehicleModelRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy model có ID = " + id));

        validateModel(requestDTO);

        existing.setModelName(requestDTO.getModelName());
        existing.setManufacturer(requestDTO.getManufacturer());
        existing.setBatteryCapacity(requestDTO.getBatteryCapacity());
        existing.setSeatingCapacity(requestDTO.getSeatingCapacity());

        VehicleModel updated = vehicleModelRepository.save(existing);
        return vehicleModelMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteVehicleModel(Long id) {
        if (!vehicleModelRepository.existsById(id)) {
            throw new CustomException("Không tìm thấy model có ID = " + id);
        }
        vehicleModelRepository.deleteById(id);
    }

    // 🧩 Validate dữ liệu cơ bản
    private void validateModel(VehicleModelRequestDTO dto) {
        if (dto.getBatteryCapacity() == null || dto.getBatteryCapacity() <= 0) {
            throw new CustomException("Dung lượng pin phải lớn hơn 0");
        }
        if (dto.getSeatingCapacity() <= 0) {
            throw new CustomException("Số ghế phải lớn hơn 0");
        }
    }
}
