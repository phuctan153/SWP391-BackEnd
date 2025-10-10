package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @ManyToOne @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne @JoinColumn(name = "model_id")
    private VehicleModel model;

    @Column(unique = true)
    private String plateNumber;

    private Double batteryLevel;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Double mileage;
    private LocalDate lastServiceDate;

    public enum Status { AVAILABLE, RESERVED, IN_USE, MAINTENANCE }

    @OneToMany(mappedBy = "vehicle")
    private List<Booking> bookings;
}

