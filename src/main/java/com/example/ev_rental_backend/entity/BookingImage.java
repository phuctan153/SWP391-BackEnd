package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_image")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne @JoinColumn(name = "booking_id")
    private Booking booking;

    private String imageUrl;
    private LocalDateTime takenAt;
    private String description;
}

