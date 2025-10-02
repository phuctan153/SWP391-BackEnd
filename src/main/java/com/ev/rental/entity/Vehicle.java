package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(name = "plate_number", unique = true, nullable = false)
    private String plateNumber;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    private Integer mileage;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    private String color;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(columnDefinition = "TEXT")
    private String images;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Booking> bookings;
}

enum VehicleStatus {
    AVAILABLE, RESERVED, IN_USE, MAINTENANCE
}
