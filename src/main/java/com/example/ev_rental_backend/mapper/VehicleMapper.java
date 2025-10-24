package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.booking.BookingHistoryDTO;
import com.example.ev_rental_backend.dto.station_vehicle.VehicleResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleRequestDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleResDTO;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.dto.vehicle.VehicleDetailResponseDTO;
import com.example.ev_rental_backend.dto.vehicle.VehicleFeedbackDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.BookingRating;
import com.example.ev_rental_backend.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "modelName", source = "model.modelName") // nếu Vehicle có quan hệ với Model
    VehicleResponseDTO toResponseDto(Vehicle vehicle);

    List<VehicleResponseDTO> toResponseDtoList(List<Vehicle> vehicles);

    @Mapping(source = "station.name", target = "stationName")
    VehicleResDTO toResponseDTO(Vehicle vehicle);

    @Mapping(target = "modelName", source = "model.modelName")
    @Mapping(target = "stationName", source = "station.name")
    VehicleDetailResponseDTO toVehicleDetailDto(Vehicle vehicle);

    // 🎯 Lịch sử cho thuê
    @Mapping(target = "renterName", source = "renter.fullName")
    @Mapping(target = "renterEmail", source = "renter.email")
    BookingHistoryDTO toBookingHistoryDto(Booking booking);

    // 🎯 Feedback
    @Mapping(target = "renterName", source = "booking.renter.fullName")
    VehicleFeedbackDTO toVehicleFeedbackDto(BookingRating rating);

    List<BookingHistoryDTO> toBookingHistoryDtoList(List<Booking> bookings);
    List<VehicleFeedbackDTO> toVehicleFeedbackDtoList(List<BookingRating> ratings);
}
