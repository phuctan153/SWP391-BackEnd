package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_station")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffStation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffStationId;

    @ManyToOne @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne @JoinColumn(name = "station_id")
    private Station station;

    private LocalDateTime assignedAt;
    private LocalDateTime unassignedAt;

    @Enumerated(EnumType.STRING)
    private RoleAtStation roleAtStation;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum RoleAtStation { STATION_STAFF, STATION_ADMIN }
    public enum Status { ACTIVE, INACTIVE }
}
