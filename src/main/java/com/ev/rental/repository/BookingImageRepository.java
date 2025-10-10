package com.ev.rental.repository;

import com.ev.rental.entity.BookingImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingImageRepository extends JpaRepository<BookingImage, String> {
}
