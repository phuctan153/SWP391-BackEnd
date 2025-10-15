package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vehicle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vehicleId;

    @Column(nullable = false, length = 100)
    private String vehicleName;

    // 🔗 Thuộc về trạm nào
    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    // 🔗 Thuộc model nào (VD: VinFast VF e34, Feliz S…)
    @ManyToOne
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    // 💰 Giá thuê
    private Double pricePerHour;
    private Double pricePerDay;

    // 🔢 Biển số
    @Column(unique = true, nullable = false, length = 20)
    private String plateNumber;

    // ⚡ Mức pin hiện tại (%)
    private Double batteryLevel;

    // 📏 Quãng đường đã đi (km)
    private Double mileage;

    // 🧾 Mô tả chi tiết về xe
    @Column(columnDefinition = "TEXT")
    private String description;

    // ⚙️ Trạng thái xe
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status;

    // 🔗 Các booking của xe
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    public enum Status {
        AVAILABLE, RESERVED, IN_USE, MAINTENANCE
    }
}
