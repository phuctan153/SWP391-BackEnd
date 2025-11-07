package com.example.ev_rental_backend.service.admin;

import com.example.ev_rental_backend.dto.admin.AdminResponseDto;
import com.example.ev_rental_backend.dto.admin.CreateAdminDto;
import com.example.ev_rental_backend.dto.admin.UpdateAdminDto;
import com.example.ev_rental_backend.entity.Admin;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.AdminRepository;
import com.example.ev_rental_backend.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * Lấy thông tin admin hiện tại
     */
    public AdminResponseDto getCurrentAdmin() {
        Admin admin = getAuthenticatedAdmin();
        return mapToResponseDto(admin);
    }

    /**
     * Cập nhật thông tin admin hiện tại
     */
    public AdminResponseDto updateCurrentAdmin(UpdateAdminDto requestDto) {
        Admin admin = getAuthenticatedAdmin();

        if (requestDto.getFullName() != null) {
            admin.setFullName(requestDto.getFullName());
        }
        if (requestDto.getEmail() != null) {
            // Check email unique
            if (adminRepository.existsByEmailAndGlobalAdminIdNot(requestDto.getEmail(), admin.getGlobalAdminId())) {
                throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
            }
            admin.setEmail(requestDto.getEmail());
        }
        if (requestDto.getPhoneNumber() != null) {
            admin.setPhoneNumber(requestDto.getPhoneNumber());
        }

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin {} updated profile", admin.getGlobalAdminId());

        return mapToResponseDto(savedAdmin);
    }

    /**
     * Lấy danh sách tất cả admin
     */
    public List<AdminResponseDto> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Tạo admin mới
     */
    public AdminResponseDto createAdmin(CreateAdminDto requestDto) {
        // Check email unique
        if (adminRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        // Check phone unique
        if (adminRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new CustomException("Phone number already exists", HttpStatus.BAD_REQUEST);
        }

        Admin admin = Admin.builder()
                .fullName(requestDto.getFullName())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .phoneNumber(requestDto.getPhoneNumber())
                .status(Admin.Status.ACTIVE)
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        log.info("New admin created: {}", savedAdmin.getEmail());

        return mapToResponseDto(savedAdmin);
    }

    /**
     * Cập nhật admin
     */
    public AdminResponseDto updateAdmin(Long adminId, UpdateAdminDto requestDto) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found with id: " + adminId));

        if (requestDto.getFullName() != null) {
            admin.setFullName(requestDto.getFullName());
        }
        if (requestDto.getEmail() != null) {
            if (adminRepository.existsByEmailAndGlobalAdminIdNot(requestDto.getEmail(), adminId)) {
                throw new CustomException("Email already exists", HttpStatus.BAD_REQUEST);
            }
            admin.setEmail(requestDto.getEmail());
        }
        if (requestDto.getPhoneNumber() != null) {
            admin.setPhoneNumber(requestDto.getPhoneNumber());
        }
        if (requestDto.getStatus() != null) {
            admin.setStatus(requestDto.getStatus());
        }

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin {} updated", adminId);

        return mapToResponseDto(savedAdmin);
    }

    // ==================== Helper Methods ====================

    private Admin getAuthenticatedAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
    }

    private AdminResponseDto mapToResponseDto(Admin admin) {
        // Đếm số hợp đồng đã ký
        Integer totalContracts = contractRepository.countByAdmin_GlobalAdminId(admin.getGlobalAdminId());

        return AdminResponseDto.builder()
                .globalAdminId(admin.getGlobalAdminId())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .phoneNumber(admin.getPhoneNumber())
                .status(admin.getStatus())
                .totalContracts(totalContracts)
                .build();
    }
}
