package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
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
        List<Vehicle> vehicles = vehicleRepository.findByStationId(stationId);
        return vehicleMapper.toResponseDtoList(vehicles);
    }
}
