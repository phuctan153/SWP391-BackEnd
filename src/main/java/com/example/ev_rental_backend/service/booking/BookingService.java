package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.dto.booking.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.ev_rental_backend.entity.Booking;

import java.util.List;

public interface BookingService {
    public BookingResponseDto createBooking(CreateBookingRequestDto requestDto);
    public BookingResponseDto getBookingById(Long bookingId);

    public Booking getBookingEntityById(Long bookingId);
    public BookingResponseDto cancelBooking(Long bookingId, CancelBookingRequestDto requestDto);
    public BookingImageResponseDto uploadBookingImage(Long bookingId, MultipartFile file,
                                                      String imageTypeStr, String description);
    public List<BookingImageResponseDto> getBookingImages(Long bookingId);
    public BookingResponseDto pickupVehicle(Long bookingId, PickupRequestDto requestDto);
    public BookingResponseDto updateStatusToInUse(Long bookingId);
    public ReturnResponseDto returnVehicle(Long bookingId, ReturnRequestDto requestDto);
    public BookingResponseDto completeBooking(Long bookingId);
    public BookingRatingResponseDto rateBooking(Long bookingId, CreateBookingRatingDto requestDto);
    public BookingRatingResponseDto getBookingRating(Long bookingId);

    public BookingResponseDto updateStatusToReserved(Long bookingId);
    public List<Booking> getBookingsWithDamages();

    public List<BookingResponseDto> getMyBookings(String status);
    public BookingResponseDto getMyBookingDetail(Long bookingId);

    public void sendCancellationEmailToRenter(Long bookingId);

    List<BookingWithContractDTO> getBookingsWithContractsByActiveStation(Long staffId);

}
