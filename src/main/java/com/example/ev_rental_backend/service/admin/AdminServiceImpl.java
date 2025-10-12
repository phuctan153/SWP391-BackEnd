package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.entity.Admin;
import com.example.ev_rental_backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public Admin loginAdmin(String email, String password) {
        // 🔍 Kiểm tra tồn tại email
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));

        // 🔒 Vì bạn CHƯA mã hóa password → dùng equals() tạm thời
        if (!password.equals(admin.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // 🚫 Kiểm tra trạng thái tài khoản
        if (admin.getStatus() == Admin.Status.INACTIVE) {
            throw new RuntimeException("Tài khoản quản trị viên đã bị vô hiệu hóa");
        }

        return admin;
    }
}
