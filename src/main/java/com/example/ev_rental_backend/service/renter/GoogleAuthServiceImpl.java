package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.config.jwt.JwtTokenUtil;
import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.login.LoginResponseDTO;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.repository.RenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final RenterRepository renterRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RenterServiceImpl renterServiceImpl;

    @Override
    public ApiResponse<LoginResponseDTO> handleGoogleLogin(String sub, String email, String name, String picture) {

        // 1️⃣ Tìm renter qua Google ID hoặc Email
        Renter renter = renterRepository.findByGoogleId(sub)
                .or(() -> renterRepository.findByEmail(email))
                .map(existing -> {
                    // Nếu renter đã tồn tại nhưng chưa có Google ID, cập nhật vào
                    if (existing.getGoogleId() == null) {
                        existing.setGoogleId(sub);
                        existing.setAuthProvider(Renter.AuthProvider.GOOGLE);
                        renterRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    // 2️⃣ Nếu chưa có → tạo renter mới từ Google
                    Renter newRenter = Renter.builder()
                            .googleId(sub)
                            .email(email)
                            .fullName(name)
                            .authProvider(Renter.AuthProvider.GOOGLE)
                            .status(Renter.Status.PENDING_VERIFICATION)
                            .isBlacklisted(false)
                            .build();

                    // 3️⃣ Tạo ví mới (INACTIVE) và liên kết 2 chiều
                    Wallet wallet = Wallet.builder()
                            .balance(BigDecimal.ZERO)
                            .status(Wallet.Status.INACTIVE)
                            .renter(newRenter)
                            .build();

                    newRenter.setWallet(wallet);

                    // ⚡ Lưu renter, cascade tự lưu luôn wallet
                    return renterRepository.save(newRenter);
                });

        // 4️⃣ Lấy trạng thái KYC hiện tại
        String kycStatus = renterServiceImpl.getKycStatusForRenter(renter);

        // 5️⃣ Xác định bước tiếp theo
        String nextStep;
        switch (kycStatus) {
            case "NEED_UPLOAD":
            case "REJECTED":
            case "UNKNOWN":
                nextStep = "KYC_UPLOAD";
                break;
            case "WAITING_APPROVAL":
            case "VERIFIED":
                nextStep = "DASHBOARD";
                break;
            default:
                nextStep = "KYC_UPLOAD";
                break;
        }

        // 6️⃣ Sinh JWT token
        String token = jwtTokenUtil.generateTokenWithRole(renter.getEmail(), "RENTER");

        // 7️⃣ Tạo DTO trả về FE
        LoginResponseDTO loginResponse = new LoginResponseDTO(token, renter.getEmail(), kycStatus);

        Map<String, Object> data = new HashMap<>();
        data.put("user", loginResponse);
        data.put("nextStep", nextStep);
        data.put("googleName", name);
        data.put("googlePicture", picture);

        // 8️⃣ Trả response chuẩn
        return ApiResponse.<LoginResponseDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(loginResponse)
                .build();
    }
}
