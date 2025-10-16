package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.mapper.VehicleMapper;
import com.example.ev_rental_backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

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

        return dto;
    }
}
