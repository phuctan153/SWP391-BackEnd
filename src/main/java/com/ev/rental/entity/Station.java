package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Long stationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String location;

    private Double latitude;

    private Double longitude;

    @Column(name = "phone_number")
    private String phoneNumber;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StationStatus status = StationStatus.ACTIVE;

    @ManyToMany(mappedBy = "stations", fetch = FetchType.LAZY)
    private java.util.List<Staff> staffList;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Vehicle> vehicles;
}

enum StationStatus {
    ACTIVE, INACTIVE
}
