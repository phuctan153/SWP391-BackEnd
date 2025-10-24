package com.example.ev_rental_backend.dto.booking;

import com.example.ev_rental_backend.entity.BookingImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingImageResponseDto {

    private Long imageId;
    private String imageUrl;
    private BookingImage.ImageType imageType;
    private String description;
    private LocalDateTime createdAt;
}
