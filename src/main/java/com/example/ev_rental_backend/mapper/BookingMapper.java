package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.booking.BookingResponseBlacklistDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.BookingImage;
import com.example.ev_rental_backend.entity.Renter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "vehicleName", source = "vehicle.vehicleName")
    @Mapping(target = "vehiclePlateNumber", source = "vehicle.plateNumber")
    @Mapping(target = "renter", expression = "java(toRenterShortInfo(booking.getRenter()))")
    @Mapping(target = "images", expression = "java(toImageDTOList(booking.getImages()))")
    BookingResponseBlacklistDTO toBlacklistDto(Booking booking);

    default BookingResponseBlacklistDTO.RenterShortInfo toRenterShortInfo(Renter renter) {
        if (renter == null) return null;
        return BookingResponseBlacklistDTO.RenterShortInfo.builder()
                .renterId(renter.getRenterId())
                .fullName(renter.getFullName())
                .email(renter.getEmail())
                .build();
    }

    default List<BookingResponseBlacklistDTO.BookingImageDTO> toImageDTOList(List<BookingImage> images) {
        if (images == null) return null;
        return images.stream()
                .map(i -> BookingResponseBlacklistDTO.BookingImageDTO.builder()
                        .imageId(i.getImageId())
                        .imageUrl(i.getImageUrl())
                        .description(i.getDescription())
                        .imageType(i.getImageType().name())
                        .build())
                .toList();
    }
}
