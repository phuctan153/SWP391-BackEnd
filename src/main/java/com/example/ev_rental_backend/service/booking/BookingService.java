package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.dto.booking.*;
import com.example.ev_rental_backend.entity.BookingImage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import com.example.ev_rental_backend.entity.Booking;

import java.util.List;
import java.util.Map;

public interface BookingService {
    public BookingResponseDto createBooking(CreateBookingRequestDto requestDto);
    public BookingResponseDto getBookingById(Long bookingId);

    public Booking getBookingEntityById(Long bookingId);
    public BookingResponseDto cancelBooking(Long bookingId, CancelBookingRequestDto requestDto);
    public BookingImageResponseDto uploadBookingImage(
            Long bookingId,
            MultipartFile file,
            String imageTypeStr,
            String vehicleComponentStr,
            String description);
    public List<BookingImageResponseDto> getBookingImages(
            Long bookingId,
            String imageTypeFilter,
            String vehicleComponentFilter);
    public BookingImageResponseDto confirmBookingImage(Long imageId);
    public void deleteBookingImage(Long bookingId, Long imageId);
    public Map<String, Object> checkImageChecklist(Long bookingId, BookingImage.ImageType imageType);

    public BookingResponseDto pickupVehicle(Long bookingId, PickupRequestDto requestDto);
    public BookingResponseDto updateStatusToInUse(Long bookingId, HttpServletRequest request);
    public ReturnResponseDto returnVehicle(Long bookingId, ReturnRequestDto requestDto, HttpServletRequest request);
    public BookingResponseDto completeBooking(Long bookingId);
    public BookingRatingResponseDto rateBooking(Long bookingId, CreateBookingRatingDto requestDto);
    public BookingRatingResponseDto getBookingRating(Long bookingId);

    public BookingResponseDto updateStatusToReserved(Long bookingId);
    public List<BookingResponseDto> getBookingsWithDamages();

    public List<BookingResponseDto> getMyBookings(String status);
    public BookingResponseDto getMyBookingDetail(Long bookingId);

    public void sendCancellationEmailToRenter(Long bookingId);

    List<BookingWithContractDTO> getBookingsWithContractsByActiveStation(Long staffId);

    List<BookingResponseDto> getAllBookings();


    void notifyStationStaffAboutReturn(Long bookingId, String renterEmail);



}
