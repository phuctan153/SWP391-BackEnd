package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.login.LoginResponseDTO;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.RenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final RenterRepository renterRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RenterServiceImpl renterServiceImpl; // ✅ Inject RenterService để gọi hàm KYC tái sử dụng

    @Override
    public ApiResponse<LoginResponseDTO> handleGoogleLogin(String sub, String email, String name, String picture) {

        // 🔹 1. Tìm user theo googleId hoặc email
        Renter renter = renterRepository.findByGoogleId(sub)
                .or(() -> renterRepository.findByEmail(email))
                .orElseGet(() -> {
                    // 🔹 2. Nếu chưa có → tạo mới renter Google
                    Renter newRenter = Renter.builder()
                            .googleId(sub)
                            .email(email)
                            .fullName(name)
                            .authProvider(Renter.AuthProvider.GOOGLE)
                            .status(Renter.Status.PENDING_VERIFICATION)
                            .isBlacklisted(false)
                            .build();
                    return renterRepository.save(newRenter);
                });

        // 🔹 3. Sinh JWT token (có role)
        String token = jwtTokenUtil.generateTokenWithRole(email, "RENTER");

        // 🔹 4. Lấy trạng thái KYC thông qua service dùng chung
        String kycStatus = renterServiceImpl.getKycStatusForRenter(renter);

        // 🔹 5. Trả response (token + email + KYC status)
        LoginResponseDTO responseDTO = new LoginResponseDTO(token, email, kycStatus);

        return ApiResponse.<LoginResponseDTO>builder()
                .status("success")
                .code(200)
                .data(responseDTO)
                .build();
    }
}
