package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.IdentityDocument;
import com.example.ev_rental_backend.entity.OtpVerificationEmail;
import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.mapper.KycMapper;
import com.example.ev_rental_backend.mapper.RenterMapper;
import com.example.ev_rental_backend.repository.IdentityDocumentRepository;
import com.example.ev_rental_backend.repository.OtpVerificationEmailRepository;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.WalletRepository;
import com.example.ev_rental_backend.service.otp.OtpEmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RenterServiceImpl implements RenterService{

    @Autowired
    RenterRepository renterRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    IdentityDocumentRepository identityDocumentRepository;

    @Autowired
    OtpVerificationEmailRepository otpVerificationEmailRepository;


    @Autowired
    KycMapper kycMapper;

    @Autowired
    RenterMapper renterMapper;

    @Autowired
    OtpEmailServiceImpl otpEmailServiceImpl;

    @Override
    public RenterResponseDTO registerRenter(RenterRequestDTO dto) {

        // 🔹 1. Kiểm tra email và số điện thoại trùng
        if (renterRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại! Vui lòng đăng nhập.");
        }
        if (renterRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng!");
        }

        // 🔹 2. Chuyển từ DTO sang Entity
        Renter renter = renterMapper.toEntity(dto);

        // 🔹 3. Thiết lập giá trị mặc định
        renter.setStatus(Renter.Status.PENDING_VERIFICATION);
        renter.setAuthProvider(Renter.AuthProvider.LOCAL);
        renter.setBlacklisted(false);
        renter.setCreatedAt(LocalDateTime.now());
        renter.setUpdatedAt(LocalDateTime.now());

        // 🔹 4. Lưu renter vào DB
        Renter savedRenter = renterRepository.save(renter);

        // 🔹 5. Tạo ví (Wallet) mặc định nếu chưa tồn tại
        if (!walletRepository.existsByRenter(savedRenter)) {
            Wallet wallet = Wallet.builder()
                    .renter(savedRenter)
                    .balance(BigDecimal.ZERO)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status(Wallet.Status.ACTIVE)
                    .build();
            walletRepository.save(wallet);
        }

        // 🔹 6. Trả về DTO phản hồi
        return renterMapper.toResponseDto(savedRenter);
    }


    @Override
    public RenterResponseDTO loginRenter(String email, String password) {
        // 🔹 1. Tìm renter theo email
        Renter renter = renterRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // 🔹 2. Kiểm tra mật khẩu
        if (!renter.getPassword().equals(password)) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // 🔹 3. Kiểm tra tài khoản bị khoá
        if (renter.isBlacklisted()) {
            throw new RuntimeException("Tài khoản của bạn hiện bị tạm khoá vì nghi ngờ hoạt động vi phạm. Vui lòng kiểm tra email để biết hướng kháng nghị.");
        }

        // 🔹 4. Kiểm tra OTP email đã xác thực chưa
        boolean isOtpVerified = otpEmailServiceImpl.isRenterVerified(renter.getRenterId());
        String otpStatus = isOtpVerified ? "VERIFIED" : "PENDING";

        // 🔹 5. Kiểm tra trạng thái KYC (CCCD + GPLX)
        String kycStatus = getKycStatusForRenter(renter);

        // 🔹 6. Xác định bước tiếp theo cho frontend
        String nextStep;
        if (!isOtpVerified) {
            nextStep = "EMAIL_OTP"; // Chưa xác minh email
        } else {
            nextStep = switch (kycStatus) {
                case "NEED_UPLOAD", "REJECTED", "UNKNOWN" -> "KYC_UPLOAD"; // Cần upload hoặc re-upload KYC
                // Tùy bạn chọn dùng tên nào
                case "WAITING_APPROVAL", "VERIFIED" ->
                        "DASHBOARD"; // Được vào dashboard (nếu pending thì chờ staff duyệt)
                default -> "KYC_UPLOAD"; // Mặc định fallback
            };
        }

        // 🔹 7. Map sang DTO phản hồi
        RenterResponseDTO response = renterMapper.toResponseDto(renter);
        response.setOtpStatus(otpStatus);
        response.setKycStatus(kycStatus);
        response.setNextStep(nextStep);

        // 🔹 8. Trả kết quả cho Controller
        return response;
    }



    @Override
    public Renter verifyKyc(KycVerificationDTO dto) {

        // 🔹 0. Kiểm tra OTP đã được xác thực hay chưa
        boolean hasVerifiedOtp = otpVerificationEmailRepository.existsVerifiedOtpForRenter(dto.getRenterId());
        if (!hasVerifiedOtp) {
            throw new RuntimeException("Renter chưa xác thực OTP. Vui lòng kiểm tra email và xác nhận trước khi gửi KYC.");
        }

        // 🔹 1. Lấy renter
        Renter renter = renterRepository.findById(dto.getRenterId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy renter với ID: " + dto.getRenterId()));

        // 🔹 2. So sánh tên
        if (!normalize(dto.getNationalName()).equalsIgnoreCase(normalize(dto.getDriverName()))) {
            throw new RuntimeException("Tên trên CCCD và GPLX không khớp nhau.");
        }

        // 🔹 3. Kiểm tra trùng CCCD / GPLX
        identityDocumentRepository.findByDocumentNumberAndType(
                        dto.getNationalId(), IdentityDocument.DocumentType.NATIONAL_ID)
                .ifPresent(doc -> {
                    if (!doc.getRenter().getRenterId().equals(dto.getRenterId())) {
                        throw new RuntimeException("CCCD này đã được sử dụng bởi người khác.");
                    }
                });

        identityDocumentRepository.findByDocumentNumberAndType(
                        dto.getDriverLicense(), IdentityDocument.DocumentType.DRIVER_LICENSE)
                .ifPresent(doc -> {
                    if (!doc.getRenter().getRenterId().equals(dto.getRenterId())) {
                        throw new RuntimeException("GPLX này đã được sử dụng bởi người khác.");
                    }
                });

        // 🔹 4. Kiểm tra hạn CCCD & GPLX
        LocalDate today = LocalDate.now();
        if (dto.getNationalExpireDate().isBefore(today))
            throw new RuntimeException("CCCD đã hết hạn.");

        if (dto.getDriverExpireDate() == null || !isValidCarLicense(dto.getDriverClass())) {
            throw new RuntimeException("Loại GPLX không hợp lệ để thuê ô tô. Cần có bằng B1 trở lên.");
        }

        if (dto.getDriverExpireDate().isBefore(today))
            throw new RuntimeException("GPLX đã hết hạn.");

        // 🔹 5. Kiểm tra tuổi
        int age = Period.between(dto.getNationalDob(), today).getYears();
        if (age < 21)
            throw new RuntimeException("Người dùng chưa đủ 21 tuổi.");

        // 🔹 6. Cập nhật thông tin vào DB
        kycMapper.updateRenterFromKyc(dto, renter);
        renter.setStatus(Renter.Status.PENDING_VERIFICATION);

        // 🔹 7. Cập nhật bảng IdentityDocument
        saveOrUpdateDocument(
                renter,
                dto.getNationalId(),
                IdentityDocument.DocumentType.NATIONAL_ID,
                dto.getNationalIssueDate(),
                dto.getNationalExpireDate(),
                dto.getNationalName()
        );

        saveOrUpdateDocument(
                renter,
                dto.getDriverLicense(),
                IdentityDocument.DocumentType.DRIVER_LICENSE,
                dto.getDriverIssueDate(),
                dto.getDriverExpireDate(),
                dto.getDriverName()
        );


        // 🔹 8. Lưu Renter
        return renterRepository.save(renter);
    }

    private void saveOrUpdateDocument(
            Renter renter,
            String documentNumber,
            IdentityDocument.DocumentType type,
            LocalDate issueDate,
            LocalDate expiryDate,
            String fullName) {

        Optional<IdentityDocument> existingDoc =
                identityDocumentRepository.findByDocumentNumberAndType(documentNumber, type);

        IdentityDocument document = existingDoc.orElseGet(() -> IdentityDocument.builder()
                .renter(renter)
                .type(type)
                .documentNumber(documentNumber)
                .build()
        );

        // 🧠 Gán các giá trị mới (bao gồm fullName)
        document.setFullName(fullName);
        document.setIssueDate(issueDate);
        document.setExpiryDate(expiryDate);
        document.setStatus(IdentityDocument.DocumentStatus.PENDING);
        document.setUpdatedAt(LocalDateTime.now());

        identityDocumentRepository.save(document);
    }





    private boolean isValidCarLicense(String driverClass) {
        if (driverClass == null || driverClass.isBlank()) return false;

        String type = driverClass.trim().toUpperCase();
        List<String> validCarLicenses = List.of("B1", "B2", "C", "D", "E", "F");

        return validCarLicenses.contains(type);
    }

    @Override
    public String checkKycStatus(Long renterId) {
        // 🔍 1. Tìm renter trong database
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter với ID: " + renterId));

        // ✅ 2. Gọi lại hàm xử lý logic KYC (để tái sử dụng)
        return getKycStatusForRenter(renter);
    }

    @Override
    public String getKycStatusForRenter(Renter renter) {
        // 📂 1. Lấy danh sách giấy tờ định danh (CCCD, GPLX, ...)
        List<IdentityDocument> docs = renter.getIdentityDocuments();

        // 🪪 2. Kiểm tra xem renter đã upload CCCD và GPLX chưa
        boolean hasCCCD = docs.stream()
                .anyMatch(d -> d.getType() == IdentityDocument.DocumentType.NATIONAL_ID);

        boolean hasGPLX = docs.stream()
                .anyMatch(d -> d.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE);

        // ❗️ Nếu thiếu 1 trong 2 loại giấy tờ → yêu cầu upload
        if (!hasCCCD || !hasGPLX) {
            return "NEED_UPLOAD";
        }

        // 🔍 3. Kiểm tra trạng thái giấy tờ
        boolean allPending = docs.stream()
                .allMatch(d -> d.getStatus() == IdentityDocument.DocumentStatus.PENDING);
        boolean anyRejected = docs.stream()
                .anyMatch(d -> d.getStatus() == IdentityDocument.DocumentStatus.REJECTED);
        boolean allVerified = docs.stream()
                .allMatch(d -> d.getStatus() == IdentityDocument.DocumentStatus.VERIFIED);

        // ❌ Có giấy tờ bị từ chối
        if (anyRejected) {
            return "REJECTED";
        }

        // ⏳ Tất cả giấy tờ đang chờ duyệt
        if (allPending) {
            return "WAITING_APPROVAL";
        }

        // ✅ Tất cả giấy tờ đã VERIFIED và renter cũng VERIFIED
        if (allVerified && renter.getStatus() == Renter.Status.VERIFIED) {
            return "VERIFIED";
        }

        // ❓ Không rơi vào bất kỳ trường hợp nào ở trên
        return "UNKNOWN";
    }

    @Override
    public RenterResponseDTO toResponseDto(Renter renter) {
        RenterResponseDTO dto = renterMapper.toResponseDto(renter);

        dto.setKycStatus(getKycStatusForRenter(renter));
        dto.setOtpStatus(
                otpEmailServiceImpl.isRenterVerified(renter.getRenterId()) ? "VERIFIED" : "PENDING"
        );
        dto.setNextStep(
                !otpEmailServiceImpl.isRenterVerified(renter.getRenterId()) ? "EMAIL_OTP" :
                        (!"VERIFIED".equals(getKycStatusForRenter(renter)) ? "KYC" : "DASHBOARD")
        );

        return dto;
    }


    @Override
    public List<RenterResponseDTO> getPendingVerificationRenters() {
        return renterRepository.findByStatus(Renter.Status.PENDING_VERIFICATION)
                .stream()
                .map(renterMapper::toResponseDto)
                .toList();
    }

    @Override
    public RenterResponseDTO verifyRenterById(Long renterId) {
        // 🔹 1. Tìm renter
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê có ID: " + renterId));

        // 🔹 2. Lấy danh sách giấy tờ
        List<IdentityDocument> docs = renter.getIdentityDocuments();
        if (docs == null || docs.isEmpty()) {
            throw new RuntimeException("Người thuê chưa gửi giấy tờ KYC.");
        }

        // 🔹 3. Cập nhật trạng thái giấy tờ
        for (IdentityDocument doc : docs) {
            if (doc.getStatus() != IdentityDocument.DocumentStatus.VERIFIED) {
                doc.setStatus(IdentityDocument.DocumentStatus.VERIFIED);
                doc.setVerifiedAt(LocalDateTime.now());
                identityDocumentRepository.save(doc);
            }
        }

        // 🔹 4. Cập nhật renter sang VERIFIED
        renter.setStatus(Renter.Status.VERIFIED);
        renter.setUpdatedAt(LocalDateTime.now());
        renterRepository.save(renter);

        // 🔹 6. Trả kết quả
        return renterMapper.toResponseDto(renter);
    }



    @Override
    public void deleteRenterById(Long renterId) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người thuê có ID: " + renterId));

        if (renter.getStatus() == Renter.Status.DELETED) {
            throw new RuntimeException("Người thuê này đã bị xóa trước đó.");
        }

        renter.setStatus(Renter.Status.DELETED);
        renterRepository.save(renter);
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