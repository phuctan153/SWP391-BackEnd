package com.ev.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_images")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;
}

enum ImageType {
    PICKUP, RETURN, DAMAGE
}
