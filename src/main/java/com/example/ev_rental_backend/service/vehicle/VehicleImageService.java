package com.example.ev_rental_backend.service.vehicle;

import org.springframework.web.multipart.MultipartFile;

public interface VehicleImageService {
    public String uploadVehicleImage(Long vehicleId, MultipartFile file);
}
