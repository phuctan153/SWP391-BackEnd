package com.example.ev_rental_backend.service.staff;


import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.staff.StaffDetailDTO;
import com.example.ev_rental_backend.dto.staff.StaffListDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.BookingRating;
import com.example.ev_rental_backend.entity.Staff;
import com.example.ev_rental_backend.entity.StaffStation;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.StaffRepository;
import com.example.ev_rental_backend.repository.StaffStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    StaffStationRepository staffStationRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Override
    public Staff loginStaff(String email, String password) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // So sánh mật khẩu
        if (!password.equals(staff.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // ✅ Cho phép đăng nhập, chỉ cần set lại trạng thái ACTIVE
        staff.setStatus(Staff.Status.ACTIVE);
        staffRepository.save(staff);

        return staff;
    }

    @Override
    public void logoutStaff(String token) {
        // ✅ Lấy email từ JWT token
        String email = jwtTokenUtil.extractEmail(token);

        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có email: " + email));

        // ✅ Cập nhật trạng thái
        staff.setStatus(Staff.Status.INACTIVE);
        staffRepository.save(staff);

        System.out.println("🔻 Staff " + email + " đã đăng xuất và chuyển sang INACTIVE");
    }

    @Override
    public List<StaffListDTO> getStaffByStation(Long stationId) {
        List<Object[]> results = staffRepository.findStaffWithCompletedBookingCount(stationId);
        return results.stream()
                .map(row -> {
                    Staff staff = (Staff) row[0];
                    Long count = (Long) row[1];
                    return new StaffListDTO(
                            staff.getStaffId(),
                            staff.getFullName(),
                            staff.getEmail(),
                            staff.getPhoneNumber(),
                            count
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public StaffDetailDTO getStaffDetail(Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + staffId));

        List<Booking> completedBookings = bookingRepository.findByStaff_StaffIdAndStatus(staffId, Booking.Status.COMPLETED);

        // Tính rating trung bình (nếu có)
        double avgRating = completedBookings.stream()
                .map(Booking::getBookingRating)
                .filter(r -> r != null)
                .mapToInt(BookingRating::getStaffRating)
                .average()
                .orElse(0.0);

        return StaffDetailDTO.builder()
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .phoneNumber(staff.getPhoneNumber())
                .status(staff.getStatus().name())
                .totalCompleted(completedBookings.size())
                .averageRating(avgRating)
                .build();
    }

    @Override
    public String getCurrentRoleAtStation(Long staffId) {
        return staffStationRepository.findFirstByStaff_StaffIdAndStatusOrderByAssignedAtDesc(
                        staffId, StaffStation.Status.ACTIVE
                ).map(staffStation -> staffStation.getRoleAtStation().name())
                .orElse("UNKNOWN");
    }

}
