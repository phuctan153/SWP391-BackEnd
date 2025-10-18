package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.login.LoginRequestDTO;
import com.example.ev_rental_backend.dto.login.LoginResponseDTO;
import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Admin;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Staff;
import com.example.ev_rental_backend.service.admin.AdminService;
import com.example.ev_rental_backend.service.admin.AdminServiceImpl;
import com.example.ev_rental_backend.service.renter.RenterService;
import com.example.ev_rental_backend.service.renter.RenterServiceImpl;
import com.example.ev_rental_backend.service.staff.StaffService;
import com.example.ev_rental_backend.service.staff.StaffServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RegisterController {

    @Autowired
    RenterService renterService;

    @Autowired
    StaffService staffService;

    @Autowired
    AdminService adminService;


    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody RenterRequestDTO renterRequestDTO) {
        try {
            RenterResponseDTO renter = renterService.registerRenter(renterRequestDTO);

            ApiResponse<RenterResponseDTO> response = ApiResponse.<RenterResponseDTO>builder()
                    .status("success")
                    .code(HttpStatus.CREATED.value())
                    .data(renter)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException ex) {
            // ❗ Bắt các lỗi do logic (email, phone trùng)
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .data(ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception ex) {
            // ❗ Bắt các lỗi bất ngờ khác (DB, server, mapping,...)
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .data("Đã xảy ra lỗi máy chủ: " + ex.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyKyc(@RequestBody @Valid KycVerificationDTO dto) {
        try {
            // 🔹 1. Gọi service xử lý xác thực KYC
            Renter verified = renterService.verifyKyc(dto);

            // 🔹 2. Chuyển entity sang DTO (để tránh leak dữ liệu)
            RenterResponseDTO renterDto = renterService.toResponseDto(verified);

            // 🔹 3. Bổ sung thông tin trạng thái KYC
            String kycStatus = renterService.getKycStatusForRenter(verified);
            renterDto.setKycStatus(kycStatus);

            // 🔹 4. Trả về phản hồi dạng chuẩn
            return ResponseEntity.ok(
                    ApiResponse.<RenterResponseDTO>builder()
                            .status("success")
                            .code(200)
                            .data(renterDto)
                            .build()
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(400)
                            .data(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<String>builder()
                            .status("error")
                            .code(500)
                            .data("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }






    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // 1️⃣ Kiểm tra thông tin đăng nhập
            RenterResponseDTO renter = renterService.loginRenter(loginRequest.getEmail(), loginRequest.getPassword());

            // 2️⃣ Sinh JWT token
            String token = jwtTokenUtil.generateTokenWithRole(renter.getEmail(), "RENTER");

            // 3️⃣ Lấy trạng thái KYC
            String kycStatus = renterService.checkKycStatus(renter.getRenterId());

            // 4️⃣ Tạo DTO đăng nhập chung (staff/admin/renter đều dùng được)
            LoginResponseDTO authResponse = new LoginResponseDTO(token, renter.getEmail(), kycStatus);

            // 5️⃣ Dữ liệu trả về cho renter — gói thêm nextStep
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", authResponse);
            responseData.put("nextStep", renter.getNextStep()); // ✅ thêm riêng field này

            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                    .status("success")
                    .code(200)
                    .data(responseData)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(401)
                    .data(e.getMessage())
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
    }


    @PostMapping("/login/staff")
    public ResponseEntity<ApiResponse<?>> loginStaff(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // 1️⃣ Xác thực thông tin đăng nhập
            Staff staff = staffService.loginStaff(loginRequest.getEmail(), loginRequest.getPassword());

            // 2️⃣ Sinh JWT token có role STAFF
            String token = jwtTokenUtil.generateTokenWithRole(staff.getEmail(), "STAFF");

            // 3️⃣ Chuẩn bị phản hồi
            LoginResponseDTO authResponse = new LoginResponseDTO(token, staff.getEmail(), staff.getStatus().name());

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status("success")
                    .code(200)
                    .data(authResponse)
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(401)
                    .data(e.getMessage())
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/login/admin")
    public ResponseEntity<ApiResponse<?>> loginAdmin(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            // 1️⃣ Xác thực thông tin đăng nhập
            Admin admin = adminService.loginAdmin(loginRequest.getEmail(), loginRequest.getPassword());

            // 2️⃣ Sinh JWT token có role ADMIN
            String token = jwtTokenUtil.generateTokenWithRole(admin.getEmail(), "ADMIN");

            // 3️⃣ Gộp thông tin trả về
            LoginResponseDTO authResponse = new LoginResponseDTO(token, admin.getEmail(), admin.getStatus().name());

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status("success")
                    .code(200)
                    .data(authResponse)
                    .build();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                    .status("error")
                    .code(401)
                    .data(e.getMessage())
                    .build();
            return ResponseEntity.status(401).body(errorResponse);
        }
    }




}