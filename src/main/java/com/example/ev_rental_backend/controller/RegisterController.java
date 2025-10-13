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
import com.example.ev_rental_backend.service.admin.AdminServiceImpl;
import com.example.ev_rental_backend.service.renter.RenterServiceImpl;
import com.example.ev_rental_backend.service.staff.StaffService;
import com.example.ev_rental_backend.service.staff.StaffServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RegisterController {

    @Autowired
    RenterServiceImpl renterServiceImpl;

    @Autowired
    StaffServiceImpl staffServiceImpl;

    @Autowired
    AdminServiceImpl adminServiceImpl;


    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RenterResponseDTO>> registerUser(@Valid @RequestBody RenterRequestDTO renterRequestDTO) {
//        try {
//            RenterResponseDTO renter = renterServiceImpl.registerRenter(renterRequestDTO);
//
//            ApiResponse<RenterResponseDTO> response = ApiResponse.<RenterResponseDTO>builder()
//                    .status("success")
//                    .code(200)
//                    .data(renter)
//                    .build();
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            ApiResponse<String> errorResponse = ApiResponse.<String>builder()
//                    .status("error")
//                    .code(400)
//                    .data(e.getMessage())
//                    .build();
//
//            return ResponseEntity.badRequest().body((ApiResponse) errorResponse);
//        }
        RenterResponseDTO renter = renterServiceImpl.registerRenter(renterRequestDTO);

        ApiResponse<RenterResponseDTO> response = ApiResponse.<RenterResponseDTO>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(renter)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyKyc(@RequestBody KycVerificationDTO dto) {
        try {
            Renter verified = renterServiceImpl.verifyKyc(dto);
            return ResponseEntity.ok(
                    ApiResponse.<Renter>builder()
                            .status("success")
                            .code(200)
                            .data(verified)
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
            RenterResponseDTO renter = renterServiceImpl.loginRenter(loginRequest.getEmail(), loginRequest.getPassword());

            // 2️⃣ Sinh JWT token có role RENTER
            String token = jwtTokenUtil.generateTokenWithRole(renter.getEmail(), "RENTER");

            // 3️⃣ Kiểm tra trạng thái KYC
            String kycStatus = renterServiceImpl.checkKycStatus(renter.getRenterId());

            // 4️⃣ Trả phản hồi
            LoginResponseDTO authResponse = new LoginResponseDTO(token, renter.getEmail(), kycStatus);

            ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                    .status("success")
                    .code(200)
                    .data(authResponse)
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
            Staff staff = staffServiceImpl.loginStaff(loginRequest.getEmail(), loginRequest.getPassword());

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
            Admin admin = adminServiceImpl.loginAdmin(loginRequest.getEmail(), loginRequest.getPassword());

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
