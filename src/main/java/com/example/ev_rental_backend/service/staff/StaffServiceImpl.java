package com.example.ev_rental_backend.service.staff;


import com.example.ev_rental_backend.entity.Staff;
import com.example.ev_rental_backend.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    StaffRepository staffRepository;

    @Override
    public Staff loginStaff(String email, String password) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // 🔹 So sánh chuỗi đơn giản (vì password chưa mã hóa)
        if (!password.equals(staff.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        if (staff.getStatus() == Staff.Status.INACTIVE) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa, vui lòng liên hệ quản trị viên");
        }

        return staff;
    }

}
