package com.example.ev_rental_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_rating")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingRating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ratingId;

    @OneToOne @JoinColumn(name = "booking_id")
    private Booking booking;

    private int renterRating;
    private int staffRating;
    private String comment;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
