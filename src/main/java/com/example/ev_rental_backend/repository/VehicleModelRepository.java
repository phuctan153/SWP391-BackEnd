package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {
    boolean existsByModelNameIgnoreCase(String modelName);
}
