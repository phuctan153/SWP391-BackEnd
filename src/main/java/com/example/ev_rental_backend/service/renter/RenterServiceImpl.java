package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.mapper.KycMapper;
import com.example.ev_rental_backend.mapper.RenterMapper;
import com.example.ev_rental_backend.repository.RenterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RenterServiceImpl implements RenterService{

    @Autowired
    RenterRepository renterRepository;

    @Autowired
    KycMapper kycMapper;

    @Autowired
    RenterMapper renterMapper;

    public RenterResponseDTO registerRenter(RenterRequestDTO dto) {

        // 🔹 Kiểm tra email và số điện thoại trùng
        if (renterRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        if (renterRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }

        // 🔹 Chuyển từ DTO sang Entity
        Renter renter = renterMapper.toEntity(dto);

        // 🔹 Thiết lập các giá trị mặc định
        renter.setStatus(Renter.Status.PENDING_VERIFICATION);
        renter.setAuthProvider(Renter.AuthProvider.LOCAL);
        renter.setBlacklisted(false);

        // 🔹 Lưu DB
        Renter saved = renterRepository.save(renter);

        // 🔹 Trả về DTO phản hồi
        return renterMapper.toResponseDto(saved);
    }

    @Override
    public RenterResponseDTO loginRenter(String email, String password) {
        Optional<Renter> renterOpt = renterRepository.findByEmail(email);
        if (renterOpt.isEmpty())
            throw new RuntimeException("Email không tồn tại");

        Renter renter = renterOpt.get();

        // ⚠️ Tạm thời so sánh trực tiếp (chưa mã hoá)
        if (!renter.getPassword().equals(password))
            throw new RuntimeException("Mật khẩu không chính xác");

        return renterMapper.toResponseDto(renter);
    }

    @Override
    public Renter verifyKyc(KycVerificationDTO dto) {

        // 🔹 1. Lấy renter
        Renter renter = renterRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy renter với ID: " + dto.getRenterId()));

        // 🔹 2. So sánh tên
        if (!normalize(dto.getNationalName()).equalsIgnoreCase(normalize(dto.getDriverName()))) {
            throw new RuntimeException("Tên trên CCCD và GPLX không khớp nhau.");
        }

        // 🔹 3. Kiểm tra trùng CCCD / GPLX
        renterRepository.findByNationalId(dto.getNationalId())
                .ifPresent(r -> {
                    if (!r.getRenterId().equals(dto.getRenterId())) {
                        throw new RuntimeException("CCCD này đã được sử dụng bởi người khác.");
                    }
                });

        renterRepository.findByDriverLicense(dto.getDriverLicense())
                .ifPresent(r -> {
                    if (!r.getRenterId().equals(dto.getRenterId())) {
                        throw new RuntimeException("GPLX này đã được sử dụng bởi người khác.");
                    }
                });

        // 🔹 4. Kiểm tra hạn CCCD & GPLX
        LocalDate today = LocalDate.now();
        if (dto.getNationalExpireDate().isBefore(today))
            throw new RuntimeException("CCCD đã hết hạn.");
        if (dto.getDriverExpireDate().isBefore(today))
            throw new RuntimeException("GPLX đã hết hạn.");

        // 🔹 5. Kiểm tra tuổi
        int age = Period.between(dto.getNationalDob(), today).getYears();
        if (age < 21)
            throw new RuntimeException("Người dùng chưa đủ 21 tuổi.");

        // 🔹 6. Cập nhật thông tin vào DB
        kycMapper.updateRenterFromKyc(dto, renter);

        // 🔹 7. Lưu vào DB
        return renterRepository.save(renter);
    }

    @Override
    public String checkKycStatus(Long renterId) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với ID: " + renterId));

        boolean hasCCCD = renter.getNationalId() != null && !renter.getNationalId().isEmpty();
        boolean hasGPLX = renter.getDriverLicense() != null && !renter.getDriverLicense().isEmpty();

        if (!hasCCCD || !hasGPLX) {
            return "NEED_UPLOAD"; // ❗️Cần upload CCCD + GPLX
        }

        if (renter.getStatus() == Renter.Status.PENDING_VERIFICATION) {
            return "WAITING_APPROVAL"; // ⏳ Đã upload, chờ admin duyệt
        }

        if (renter.getStatus() == Renter.Status.VERIFIED) {
            return "VERIFIED"; // ✅ Đã xác thực
        }

        return "UNKNOWN";
    }

    private String normalize(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
