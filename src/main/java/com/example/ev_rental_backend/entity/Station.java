package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "station")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Station {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    private String name;
    private String location;
    private Double latitude;
    private Double longitude;
    private int car_number;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status { ACTIVE, INACTIVE }

    @OneToMany(mappedBy = "station")
    private List<Vehicle> vehicles;

    @OneToMany(mappedBy = "station")
    private List<StaffStation> staffStations;
}

