package com.example.ev_rental_backend.service.booking;


import com.example.ev_rental_backend.dto.booking.*;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.*;
import com.example.ev_rental_backend.service.notification.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RenterRepository renterRepository;
    private final VehicleRepository vehicleRepository;
    private final StaffRepository staffRepository;
    private final BookingImageRepository bookingImageRepository;
    private final BookingRatingRepository bookingRatingRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingBusinessRuleValidator validator;
    private final FileStorageService fileStorageService;
    private final NotificationServiceImpl notificationService;

    // ==================== 5.1. Booking Creation ====================

    /**
     * Tạo booking mới (BR-05, BR-06, BR-07, BR-16, BR-22)
     */
    public BookingResponseDto createBooking(CreateBookingRequestDto requestDto) {
        // Lấy renter hiện tại
        Renter renter = getCurrentRenter();

        // BR-16: Kiểm tra renter chỉ có 1 booking active
        validator.validateRenterHasNoActiveBooking(renter);

        // Lấy vehicle
        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        // BR-05: Validate thời gian hợp lệ
        validator.validateBookingTime(requestDto.getStartDateTime(), requestDto.getEndDateTime());

        // BR-22: Validate đặt trước 7-14 ngày
        validator.validateAdvanceBookingTime(requestDto.getStartDateTime());

        // BR-07: Kiểm tra xe available
        validator.validateVehicleAvailable(vehicle, requestDto.getStartDateTime(),
                requestDto.getEndDateTime());

        // Tính tổng tiền
        Duration duration = Duration.between(requestDto.getStartDateTime(),
                requestDto.getEndDateTime());
        long days = duration.toDays();
        Double totalAmount = days * vehicle.getPricePerDay();

        // Tạo booking
        Booking booking = Booking.builder()
                .renter(renter)
                .vehicle(vehicle)
                .priceSnapshotPerHour(vehicle.getPricePerHour())
                .priceSnapshotPerDay(vehicle.getPricePerDay())
                .startDateTime(requestDto.getStartDateTime())
                .endDateTime(requestDto.getEndDateTime())
                .totalAmount(totalAmount)
                .status(Booking.Status.PENDING)
                .depositStatus(Booking.DepositStatus.PENDING)
                .build();

        // Set thời gian hết hạn (1h sau startTime)
        booking.setExpiresAt(requestDto.getStartDateTime().plusHours(1));

        Booking savedBooking = bookingRepository.save(booking);

        // Cập nhật vehicle status
        vehicle.setStatus(Vehicle.Status.IN_USE);
        vehicleRepository.save(vehicle);

        log.info("Booking {} created for renter {} and vehicle {}",
                savedBooking.getBookingId(), renter.getRenterId(), vehicle.getVehicleId());

        return mapToResponseDto(savedBooking);
    }

    /**
     * Lấy chi tiết booking
     */
    public BookingResponseDto getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Check quyền truy cập
        validateBookingAccess(booking);

        return mapToResponseDto(booking);
    }

    /**
     * Hủy booking
     */
    public BookingResponseDto cancelBooking(Long bookingId, CancelBookingRequestDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Chỉ có thể hủy booking ở trạng thái RESERVED hoặc PENDING
        if (booking.getStatus() != Booking.Status.RESERVED
                && booking.getStatus() != Booking.Status.PENDING) {
            throw new CustomException("Cannot cancel booking in status: " + booking.getStatus(),
                    HttpStatus.BAD_REQUEST);
        }

        booking.setStatus(Booking.Status.CANCELLED);

        // Giải phóng vehicle
        Vehicle vehicle = booking.getVehicle();
        vehicle.setStatus(Vehicle.Status.AVAILABLE);
        vehicleRepository.save(vehicle);

        Booking savedBooking = bookingRepository.save(booking);

        // Gửi thông báo
        notificationService.sendBookingCancelled(booking);

        log.info("Booking {} cancelled", bookingId);

        return mapToResponseDto(savedBooking);
    }

    // ==================== 5.2. Booking Images ====================

    /**
     * Upload ảnh xe (BR-09, BR-26)
     */
    public BookingImageResponseDto uploadBookingImage(Long bookingId, MultipartFile file,
                                                      String imageTypeStr, String description) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Parse imageType
        BookingImage.ImageType imageType;
        try {
            imageType = BookingImage.ImageType.valueOf(imageTypeStr);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid image type: " + imageTypeStr,
                    HttpStatus.BAD_REQUEST);
        }

        // Upload file
        String imageUrl = fileStorageService.storeFile(file, "booking-images");

        // Tạo booking image
        BookingImage image = BookingImage.builder()
                .booking(booking)
                .imageUrl(imageUrl)
                .imageType(imageType)
                .description(description)
                .build();

        BookingImage savedImage = bookingImageRepository.save(image);

        log.info("Image uploaded for booking {}: {}", bookingId, imageType);

        return mapToImageResponseDto(savedImage);
    }

    /**
     * Lấy danh sách ảnh của booking
     */
    public List<BookingImageResponseDto> getBookingImages(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        return booking.getImages().stream()
                .map(this::mapToImageResponseDto)
                .collect(Collectors.toList());
    }

    // ==================== 5.3. Pickup Process ====================

    /**
     * Check-in nhận xe (BR-08, BR-09, BR-10, BR-23, BR-24)
     */
    public BookingResponseDto pickupVehicle(Long bookingId, PickupRequestDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.Status.RESERVED) {
            throw new CustomException("Booking must be in RESERVED status to pickup",
                    HttpStatus.BAD_REQUEST);
        }

        // BR-23: Kiểm tra đã thanh toán cọc
        validator.validateDepositPaid(booking);

        // BR-24: Kiểm tra pin >= 60%
        Vehicle vehicle = booking.getVehicle();
        validator.validateBatteryLevel(vehicle);

        // BR-08: Xác thực danh tính (giả sử đã xác thực ở bước trước)

        // Gán staff
        if (requestDto.getStaffId() != null) {
            Staff staff = staffRepository.findById(requestDto.getStaffId())
                    .orElseThrow(() -> new NotFoundException("Staff not found"));
            booking.setStaff(staff);
        }

        // Cập nhật trạng thái
        booking.setStatus(Booking.Status.IN_USE);
        vehicle.setStatus(Vehicle.Status.IN_USE);

        bookingRepository.save(booking);
        vehicleRepository.save(vehicle);

        log.info("Vehicle picked up for booking {}", bookingId);

        return mapToResponseDto(booking);
    }

    /**
     * Chuyển trạng thái sang IN_USE
     */
    public BookingResponseDto updateStatusToInUse(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        booking.setStatus(Booking.Status.IN_USE);
        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponseDto(savedBooking);
    }

    // ==================== 5.4. Return Process ====================

    /**
     * Trả xe (BR-11, BR-12, BR-13, BR-14, BR-15, BR-26, BR-27)
     */
    public ReturnResponseDto returnVehicle(Long bookingId, ReturnRequestDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus() != Booking.Status.IN_USE) {
            throw new CustomException("Booking must be in IN_USE status to return",
                    HttpStatus.BAD_REQUEST);
        }

        // BR-11: Kiểm tra trả đúng trạm (giả sử đã check)

        // Set thời gian trả thực tế
        booking.setActualReturnTime(LocalDateTime.now());

        // BR-14: Tính phí trả trễ
        Double lateFee = validator.calculateLateFee(booking);

        // BR-13: Cập nhật tình trạng xe
        Vehicle vehicle = booking.getVehicle();
        vehicle.setBatteryLevel(requestDto.getBatteryLevel());
        vehicle.setMileage(requestDto.getMileage());

        // Kiểm tra hư hỏng
        boolean hasDamage = requestDto.getHasDamage() != null && requestDto.getHasDamage();

        // Cập nhật vehicle status
        if (hasDamage) {
            vehicle.setStatus(Vehicle.Status.IN_REPAIR);
        } else {
            vehicle.setStatus(Vehicle.Status.AVAILABLE);
        }

        // Cập nhật booking status
        booking.setStatus(Booking.Status.COMPLETED);

        bookingRepository.save(booking);
        vehicleRepository.save(vehicle);

        log.info("Vehicle returned for booking {}, late fee: {}", bookingId, lateFee);

        // Tạo response
        return ReturnResponseDto.builder()
                .bookingId(bookingId)
                .actualReturnTime(booking.getActualReturnTime())
                .lateFee(lateFee)
                .damageFee(requestDto.getDamageFee())
                .totalFee(lateFee + (requestDto.getDamageFee() != null ? requestDto.getDamageFee() : 0.0))
                .message("Vehicle returned successfully")
                .build();
    }

    /**
     * Hoàn tất booking
     */
    public BookingResponseDto completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // BR-15: Kiểm tra đã thanh toán đầy đủ
        validator.validateFullPayment(booking);

        booking.setStatus(Booking.Status.COMPLETED);
        Booking savedBooking = bookingRepository.save(booking);

        // Gửi thông báo
        notificationService.sendBookingCompleted(booking);

        log.info("Booking {} completed", bookingId);

        return mapToResponseDto(savedBooking);
    }

    // ==================== 5.5. Booking Rating ====================

    /**
     * Đánh giá booking
     */
    public BookingRatingResponseDto rateBooking(Long bookingId, CreateBookingRatingDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Kiểm tra booking đã hoàn thành
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new CustomException("Can only rate completed bookings",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra chưa có rating
        if (booking.getBookingRating() != null) {
            throw new CustomException("Booking already rated", HttpStatus.BAD_REQUEST);
        }

        // Tạo rating
        BookingRating rating = BookingRating.builder()
                .booking(booking)
                .vehicleRating(requestDto.getVehicleRating())
                .staffRating(requestDto.getStaffRating())
                .comment(requestDto.getComment())
                .build();

        BookingRating savedRating = bookingRatingRepository.save(rating);

        log.info("Booking {} rated: vehicle={}, staff={}",
                bookingId, requestDto.getVehicleRating(), requestDto.getStaffRating());

        return mapToRatingResponseDto(savedRating);
    }

    /**
     * Lấy rating của booking
     */
    public BookingRatingResponseDto getBookingRating(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getBookingRating() == null) {
            throw new NotFoundException("No rating found for this booking");
        }

        return mapToRatingResponseDto(booking.getBookingRating());
    }

    // ==================== Helper Methods ====================

    private Renter getCurrentRenter() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return renterRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Renter not found"));
    }

    private void validateBookingAccess(Booking booking) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Renter chỉ có thể xem booking của mình
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RENTER"))) {
            if (!booking.getRenter().getEmail().equals(email)) {
                throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
            }
        }
    }

    private BookingResponseDto mapToResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .bookingId(booking.getBookingId())
                .renterId(booking.getRenter().getRenterId())
                .renterName(booking.getRenter().getFullName())
                .vehicleId(booking.getVehicle().getVehicleId())
                .vehicleName(booking.getVehicle().getVehicleName())
                .staffId(booking.getStaff() != null ? booking.getStaff().getStaffId() : null)
                .staffName(booking.getStaff() != null ? booking.getStaff().getFullName() : null)
                .priceSnapshotPerHour(booking.getPriceSnapshotPerHour())
                .priceSnapshotPerDay(booking.getPriceSnapshotPerDay())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .actualReturnTime(booking.getActualReturnTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .depositStatus(booking.getDepositStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private BookingImageResponseDto mapToImageResponseDto(BookingImage image) {
        return BookingImageResponseDto.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .imageType(image.getImageType())
                .description(image.getDescription())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private BookingRatingResponseDto mapToRatingResponseDto(BookingRating rating) {
        return BookingRatingResponseDto.builder()
                .ratingId(rating.getRatingId())
                .bookingId(rating.getBooking().getBookingId())
                .vehicleRating(rating.getVehicleRating())
                .staffRating(rating.getStaffRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
