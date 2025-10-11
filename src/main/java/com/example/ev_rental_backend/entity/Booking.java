package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne @JoinColumn(name = "renter_id")
    private Renter renter;

    @ManyToOne @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private Double priceSnapshotPerHour;
    private Double priceSnapshotPerDay;

    private LocalDateTime actualReturnTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Double depositAmount;
    private Double totalAmount;

    public enum Status { RESERVED, IN_USE, COMPLETED, CANCELLED, EXPIRED }

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Contract contract;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Invoice> invoices;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingImage> images;
}

