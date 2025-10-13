package com.example.ev_rental_backend.service.booking;

import com.example.ev_rental_backend.dto.booking.BookingPriceRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingPriceResponseDTO;
import com.example.ev_rental_backend.dto.booking.BookingRequestDTO;
import com.example.ev_rental_backend.dto.booking.BookingResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Station;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.mapper.BookingMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.StationRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final RenterRepository renterRepository;
    private final StationRepository stationRepository;
    private final BookingMapper bookingMapper;


    @Override
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO ) {

        // 1️⃣ Kiểm tra thời gian hợp lệ
        if (requestDTO.getStartDateTime().isAfter(requestDTO.getEndDateTime())) {
            throw new CustomException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        // 2️⃣ Kiểm tra người thuê có tồn tại
        Renter renter = renterRepository.findById(requestDTO.getRenterId())
                .orElseThrow(() -> new CustomException("Không tìm thấy người thuê"));

        // 3️⃣ Kiểm tra người thuê có booking đang hoạt động chưa
        boolean hasActiveBooking = bookingRepository.existsByRenterAndStatusIn(
                renter, List.of(Booking.Status.PENDING, Booking.Status.RESERVED, Booking.Status.IN_USE)
        );
        if (hasActiveBooking) {
            throw new CustomException("Người thuê hiện có booking đang hoạt động, không thể đặt thêm");
        }

        // 4️⃣ Kiểm tra xe có tồn tại và sẵn sàng không
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new CustomException("Không tìm thấy xe với ID: " + requestDTO.getVehicleId()));

        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus().toString())) {
            throw new CustomException("Xe hiện không sẵn sàng để đặt");
        }

        // 5️⃣ Kiểm tra trạm có tồn tại
        Station station = stationRepository.findById(requestDTO.getStationId())
                .orElseThrow(() -> new CustomException("Không tìm thấy trạm xuất phát"));

        // 6️⃣ Map DTO → Entity
        Booking booking = bookingMapper.toEntity(requestDTO);
        booking.setRenter(renter);
        booking.setVehicle(vehicle);
        booking.setPriceSnapshotPerHour(vehicle.getPricePerHour());
        booking.setPriceSnapshotPerDay(vehicle.getPricePerDay());
        booking.setStatus(Booking.Status.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        // ✅ Tính giá nội bộ hoặc validate lại
        double total = calculatePrice(
                new BookingPriceRequestDTO(
                        vehicle.getVehicleId(),
                        requestDTO.getStartDateTime(),
                        requestDTO.getEndDateTime()
                )
        ).getTotalAmount();

        booking.setTotalAmount(total);

        // 7️⃣ Lưu booking
        Booking savedBooking = bookingRepository.save(booking);

        // 8️⃣ Map Entity → ResponseDTO
        return bookingMapper.toResponseDTO(savedBooking);
    }

    @Override
    public BookingPriceResponseDTO calculatePrice(BookingPriceRequestDTO requestDTO) {
        // 1️⃣ Validate đầu vào
        if (requestDTO.getStartDateTime() == null || requestDTO.getEndDateTime() == null) {
            throw new CustomException("Vui lòng chọn thời gian bắt đầu và kết thúc hợp lệ");
        }
        if (requestDTO.getEndDateTime().isBefore(requestDTO.getStartDateTime())) {
            throw new CustomException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }

        // 2️⃣ Lấy thông tin xe
        Vehicle vehicle = vehicleRepository.findById(requestDTO.getVehicleId())
                .orElseThrow(() -> new CustomException("Không tìm thấy xe với ID: " + requestDTO.getVehicleId()));

        // 3️⃣ Tính số giờ thuê
        long totalMinutes = Duration.between(requestDTO.getStartDateTime(), requestDTO.getEndDateTime()).toMinutes();
        double totalHours = totalMinutes / 60.0;

        // 4️⃣ Tính giá (theo giờ hoặc ngày)
        double pricePerHour = vehicle.getPricePerHour();
        double pricePerDay = vehicle.getPricePerDay();
        double totalAmount;

        if (totalHours >= 24) {
            double totalDays = Math.ceil(totalHours / 24.0);
            totalAmount = totalDays * pricePerDay;
        } else {
            totalAmount = totalHours * pricePerHour;
        }

        // 5️⃣ Trả về kết quả
        return BookingPriceResponseDTO.builder()
                .totalAmount(totalAmount)
                .totalHours(totalHours)
                .pricePerHour(pricePerHour)
                .pricePerDay(pricePerDay)
                .message("Tính giá thành công")
                .build();
    }
}
