package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "vehicle_model")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modelId;

    private String modelName;
    private String manufacturer;
    private Double batteryCapacity;
    private int seatingCapacity;

    @OneToMany(mappedBy = "model")
    private List<Vehicle> vehicles;
}

