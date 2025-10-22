package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> { }