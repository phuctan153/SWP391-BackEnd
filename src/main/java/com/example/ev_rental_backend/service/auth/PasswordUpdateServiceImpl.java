package com.example.ev_rental_backend.service.auth;

import com.example.ev_rental_backend.dto.update_profile.UpdatePasswordRequest;
import com.example.ev_rental_backend.entity.Admin;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Staff;
import com.example.ev_rental_backend.repository.AdminRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordUpdateServiceImpl implements PasswordUpdateService {

    private final AdminRepository adminRepository;
    private final StaffRepository staffRepository;
    private final RenterRepository renterRepository;

    @Override
    public void updatePassword(Long userId, String role, UpdatePasswordRequest request) {
        if (request.getOldPassword() == null || request.getNewPassword() == null || request.getConfirmPassword() == null)
            throw new RuntimeException("Vui lòng nhập đầy đủ thông tin");

        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Mật khẩu xác nhận không khớp");

        switch (role.toUpperCase()) {
            case "ADMIN" -> updateAdminPassword(userId, request);
            case "STAFF" -> updateStaffPassword(userId, request);
            case "RENTER" -> updateRenterPassword(userId, request);
            default -> throw new RuntimeException("Vai trò không hợp lệ: " + role);
        }
    }

    private void updateAdminPassword(Long id, UpdatePasswordRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));

        if (!request.getOldPassword().equals(admin.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        admin.setPassword(request.getNewPassword());
        adminRepository.save(admin);
    }

    private void updateStaffPassword(Long id, UpdatePasswordRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy staff"));

        if (!request.getOldPassword().equals(staff.getPassword()))
            throw new RuntimeException("Mật khẩu cũ không đúng");

        staff.setPassword(request.getNewPassword());
        staffRepository.save(staff);
    }

    private void updateRenterPassword(Long id, UpdatePasswordRequest request) {
        Renter renter = renterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter"));

        if (!request.getOldPassword().equals(renter.getPassword()))
            throw new RuntimeException("Mật khẩu cũ không đúng");

        renter.setPassword(request.getNewPassword());
        renterRepository.save(renter);
    }
}
