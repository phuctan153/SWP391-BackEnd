package com.example.ev_rental_backend.service.station;


import com.example.ev_rental_backend.dto.station_vehicle.DispatchRequestDTO;
import com.example.ev_rental_backend.dto.station_vehicle.StationLoadDTO;
import com.example.ev_rental_backend.entity.Notification;
import com.example.ev_rental_backend.entity.Staff;
import com.example.ev_rental_backend.entity.StaffStation;
import com.example.ev_rental_backend.repository.NotificationRepository;
import com.example.ev_rental_backend.repository.StaffRepository;
import com.example.ev_rental_backend.repository.StaffStationRepository;
import com.example.ev_rental_backend.repository.StationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements  DispatchService{

    private final StationRepository stationRepository;

    private final StaffStationRepository staffStationRepository;

    private final StaffRepository staffRepository;

    private final NotificationRepository notificationRepository;

    @Override
    public List<StationLoadDTO> getOverloadedStations() {
        List<Object[]> data = stationRepository.countActiveBookingsByStation();

        return data.stream()
                .map(obj -> new StationLoadDTO(
                        (Long) obj[0],
                        (String) obj[1],
                        ((Long) obj[2]).intValue(),
                        getStaffCount((Long) obj[0])
                ))
                .filter(dto -> dto.getActiveBookings() > dto.getStaffCount() * 2) // ví dụ: quá tải nếu 1 nhân viên phụ trách >2 booking
                .toList();
    }

    @Override
    @Transactional
    public String assignStaff(DispatchRequestDTO dto) {
        // 🔹 1. Kiểm tra nhân viên tồn tại
        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có ID: " + dto.getStaffId()));

        // 🔹 2. Kiểm tra trạm đích
        var targetStation = stationRepository.findById(dto.getTargetStationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trạm có ID: " + dto.getTargetStationId()));

        // 🔹 3. Cập nhật bảng StaffStation
        StaffStation newAssign = StaffStation.builder()
                .staff(staff)
                .station(targetStation)
                .assignedAt(LocalDateTime.now())
                .roleAtStation(StaffStation.RoleAtStation.STATION_STAFF)
                .status(StaffStation.Status.ACTIVE)
                .build();

        staffStationRepository.save(newAssign);

        // 🔹 4. Tạo Notification gửi đến nhân viên
        Notification noti = Notification.builder()
                .title("Điều phối công tác")
                .message("Bạn đã được điều phối đến trạm " + targetStation.getName() + ". Vui lòng chuẩn bị nhận nhiệm vụ mới.")
                .recipientType(Notification.RecipientType.STAFF)
                .recipientId(staff.getStaffId())
                .isRead(false)
                .build();

        notificationRepository.save(noti);

        // 🔹 5. Trả thông báo phản hồi cho admin
        return "Đã điều phối nhân viên " + staff.getFullName() +
                " đến trạm " + targetStation.getName();
    }

    private int getStaffCount(Long stationId) {
        return staffStationRepository.countByStation_StationIdAndStatus(stationId, StaffStation.Status.ACTIVE);
    }
}
