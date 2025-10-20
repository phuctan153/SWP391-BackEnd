package com.example.ev_rental_backend.service.vehicle;

import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
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

import java.util.List;
import java.util.Optional;

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
