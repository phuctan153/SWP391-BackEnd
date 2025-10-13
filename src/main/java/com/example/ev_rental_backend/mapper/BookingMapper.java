package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.booking.BookingRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking toEntity(BookingRequestDTO dto);

    BookingResponseDTO toResponseDTO(Booking entity);
}
