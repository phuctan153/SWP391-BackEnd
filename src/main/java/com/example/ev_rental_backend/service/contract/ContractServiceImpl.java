package com.example.ev_rental_backend.service.contract;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.contract.AdminContractSignDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.*;
import com.example.ev_rental_backend.service.notification.NotificationService;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService{

    private final BookingRepository bookingRepository;
    private final ContractRepository contractRepository;
    private final TermConditionRepository termConditionRepository;
    private final NotificationService notificationService;
    private final PdfGeneratorService pdfGeneratorService;
    private final AdminRepository adminRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final JavaMailSender mailSender;
    private final StaffRepository staffRepository;
    private final Cloudinary cloudinary;

    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO dto, Long adminId) {

        // 🔹 1️⃣ Kiểm tra booking tồn tại
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        if (booking.getStatus() != Booking.Status.RESERVED)
            throw new RuntimeException("Booking không hợp lệ để tạo hợp đồng");

        // 🔹 2️⃣ Lấy thông tin Staff đang tạo hợp đồng
        Staff admin = staffRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên tạo hợp đồng"));

        // 🔹 3️⃣ Gán staff cho booking nếu chưa có
        if (booking.getStaff() == null) {
            booking.setStaff(admin);
            bookingRepository.save(booking);
        }

        // 🔹 4️⃣ Tạo contract entity mới
        Contract contract = Contract.builder()
                .booking(booking)
                .contractType(Contract.ContractType.valueOf(dto.getContractType().toUpperCase()))
                .contractDate(LocalDateTime.now())
                .status(Contract.Status.PENDING_ADMIN_SIGNATURE)
                .build();

        // 🔹 5️⃣ Lưu contract trước để có ID
        contractRepository.save(contract);

        // 🔹 6️⃣ Lưu điều khoản
        for (ContractRequestDTO.TermConditionDTO t : dto.getTerms()) {
            termConditionRepository.save(
                    TermCondition.builder()
                            .termNumber(t.getTermNumber())
                            .termTitle(t.getTermTitle())
                            .termContent(t.getTermContent())
                            .contract(contract)
                            .build()
            );
        }

        // 🔹 7️⃣ Render file hợp đồng (PDF/HTML)
//        String fileUrl = pdfGeneratorService.generateContractFile(contract);
//        contract.setContractFileUrl(fileUrl);
        String localFilePath = pdfGeneratorService.generateContractFile(contract);
        File file = new File(localFilePath);
        String fileUrl = uploadContractFile(file);
        contract.setContractFileUrl(fileUrl);

        // 🔹 8️⃣ Lưu lại contract có fileUrl
        contractRepository.save(contract);

        // 🔹 9️⃣ Gán contract vào booking
        booking.setContract(contract);
        bookingRepository.save(booking);

        // 🔹 🔟 Trả về DTO phản hồi
        return mapToResponse(contract);
    }



    @Override
    public BookingContractInfoDTO getBookingInfoForContract(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        Renter renter = booking.getRenter();

        // 🔍 Lấy tên renter từ giấy tờ xác minh (ưu tiên CCCD)
        Optional<IdentityDocument> verifiedDoc = renter.getIdentityDocuments().stream()
                .filter(doc -> doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED)
                .filter(doc -> doc.getType() == IdentityDocument.DocumentType.NATIONAL_ID)
                .findFirst();

        if (verifiedDoc.isEmpty()) {
            verifiedDoc = renter.getIdentityDocuments().stream()
                    .filter(doc -> doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED)
                    .filter(doc -> doc.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE)
                    .findFirst();
        }

        String renterFullName = verifiedDoc.map(IdentityDocument::getFullName)
                .orElse(renter.getFullName()); // fallback nếu chưa xác minh

        return BookingContractInfoDTO.builder()
                .bookingId(booking.getBookingId())
                .vehicleName(booking.getVehicle().getVehicleName())
                .vehiclePlate(booking.getVehicle().getPlateNumber())
                .renterName(renterFullName)
                .renterEmail(renter.getEmail())
                .renterPhone(renter.getPhoneNumber())
                .staffName(booking.getStaff().getFullName())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .pricePerHour(booking.getPriceSnapshotPerHour())
                .pricePerDay(booking.getPriceSnapshotPerDay())
                .bookingStatus(booking.getStatus().name())
                .build();
    }

    @Transactional
    @Override
    public void sendContractToAdmin(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng #" + contractId));

        if (contract.getContractFileUrl() == null) {
            throw new RuntimeException("Hợp đồng chưa có file được render. Vui lòng tạo hợp đồng trước khi gửi.");
        }

        Admin admin = adminRepository.findFirstByStatus(Admin.Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quản trị viên đang hoạt động."));
        Long adminId = admin.getGlobalAdminId();

        // 🔔 Gửi thông báo cho Admin
        notificationService.sendNotificationToAdmin(
                adminId,
                "📄 Hợp đồng mới cần ký duyệt",
                "Staff đã gửi hợp đồng #" + contractId +
                        " lên để ký duyệt.\nXem tại: " + contract.getContractFileUrl()
        );

        // 📝 Cập nhật trạng thái
        contract.setStatus(Contract.Status.PENDING_ADMIN_SIGNATURE);
        contractRepository.save(contract);
    }

    @Override
    public List<BookingContractInfoDTO> getContractsByStatus(String status) {
        try {
            Contract.Status enumStatus = Contract.Status.valueOf(status.toUpperCase());
            List<Contract> contracts = contractRepository.findByStatusOrderByContractDateDesc(enumStatus);

            return contracts.stream()
                    .map(contract -> {
                        var booking = contract.getBooking();
                        var renter = booking.getRenter();
                        var staff = booking.getStaff();

                        String renterFullName = renter.getIdentityDocuments().stream()
                                .filter(doc -> doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED)
                                .filter(doc -> doc.getType() == IdentityDocument.DocumentType.NATIONAL_ID)
                                .map(IdentityDocument::getFullName)
                                .findFirst()
                                .orElseGet(() ->
                                        renter.getIdentityDocuments().stream()
                                                .filter(doc -> doc.getStatus() == IdentityDocument.DocumentStatus.VERIFIED)
                                                .filter(doc -> doc.getType() == IdentityDocument.DocumentType.DRIVER_LICENSE)
                                                .map(IdentityDocument::getFullName)
                                                .findFirst()
                                                .orElse(renter.getFullName())
                                );

                        return BookingContractInfoDTO.builder()
                                .bookingId(booking.getBookingId())
                                .vehicleName(booking.getVehicle().getVehicleName())
                                .vehiclePlate(booking.getVehicle().getPlateNumber())
                                .renterName(renterFullName)
//                                .renterName(renter.getIdentityDocuments().)
                                .renterEmail(renter.getEmail())
                                .renterPhone(renter.getPhoneNumber())
                                .staffName(staff != null ? staff.getFullName() : null)
                                .startDateTime(booking.getStartDateTime())
                                .endDateTime(booking.getEndDateTime())
                                .pricePerHour(booking.getPriceSnapshotPerHour())
                                .pricePerDay(booking.getPriceSnapshotPerDay())
                                .bookingStatus(booking.getStatus().name())
                                .build();
                    })
                    .toList();

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    public void sendOtpForAdminSignature(Long contractId, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin #" + adminId));

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng #" + contractId));

        // 🔢 Tạo mã OTP ngẫu nhiên 6 chữ số
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // 💾 Lưu OTP vào DB (gắn với Contract)
        OtpVerification otp = OtpVerification.builder()
                .contract(contract)
                .otpCode(otpCode)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .status(OtpVerification.Status.PENDING)
                .attemptCount(0)
                .build();

        otpVerificationRepository.save(otp);

        // 📩 Gửi email OTP cho Admin
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(admin.getEmail());
            helper.setSubject("🔐 Mã OTP xác thực ký hợp đồng EV Rental");
            helper.setText("""
                Xin chào %s,
                
                Mã OTP để ký hợp đồng #%d là: %s
                Mã này có hiệu lực trong 5 phút.
                
                Trân trọng,
                EV Rental System
                """.formatted(admin.getFullName(), contractId, otpCode), false);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void verifyAdminSignature(AdminContractSignDTO dto) {
        Admin admin = adminRepository.findById(dto.getAdminId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin #" + dto.getAdminId()));

        Contract contract = contractRepository.findById(dto.getContractId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng #" + dto.getContractId()));

        // 🔐 Kiểm tra OTP
        OtpVerification otp = otpVerificationRepository
                .findTopByContractOrderByCreatedAtDesc(contract)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP."));

        if (!otp.getOtpCode().equals(dto.getOtpCode())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpVerificationRepository.save(otp);
            throw new RuntimeException("Mã OTP không đúng.");
        }

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpVerification.Status.FAILED);
            otpVerificationRepository.save(otp);
            throw new RuntimeException("Mã OTP đã hết hạn.");
        }

        // ✅ Đánh dấu OTP hợp lệ
        otp.setStatus(OtpVerification.Status.VERIFIED);
        otp.setVerifiedAt(LocalDateTime.now());
        otpVerificationRepository.save(otp);

        // ✅ Xử lý ký duyệt
        Booking booking = contract.getBooking();
        Renter renter = booking.getRenter();

        if (dto.isApproved()) {
            contract.setStatus(Contract.Status.ADMIN_SIGNED);
            contract.setAdmin(admin);
            contract.setAdminSignedAt(LocalDateTime.now());

            // 🧩 Regenerate file PDF mới (cập nhật trạng thái ADMIN_SIGNED)
            String newFileUrl = pdfGeneratorService.generateContractFile(contract);
            contract.setContractFileUrl(newFileUrl);

            // 💾 Lưu sau khi có file
            contractRepository.save(contract);

            // 📧 Thông báo cho renter
            sendEmail(
                    renter.getEmail(),
                    "✅ Xe của bạn đã sẵn sàng",
                    """
                    Xin chào %s,
    
                    Hợp đồng #%d đã được quản trị viên ký duyệt thành công.
                    Xe của bạn đã sẵn sàng để nhận tại trạm thuê.
    
                    Trân trọng,
                    EV Rental System
                    """.formatted(renter.getFullName(), contract.getContractId())
            );

        } else {
            // ❌ Trường hợp bị từ chối
            contract.setStatus(Contract.Status.CANCELLED);
            booking.setStatus(Booking.Status.CANCELLED);
            bookingRepository.save(booking);

            contractRepository.save(contract);

            sendEmail(
                    renter.getEmail(),
                    "❌ Booking của bạn không được phê duyệt",
                    """
                    Xin chào %s,
    
                    Đơn đặt xe #%d của bạn đã không được kiểm duyệt.
                    Tiền cọc sẽ được hoàn lại trong vòng 3 ngày làm việc.
    
                    Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.
    
                    Trân trọng,
                    EV Rental System
                    """.formatted(renter.getFullName(), booking.getBookingId())
            );
        }
    }


    @Override
    @Transactional
    public void sendOtpToRenter(Long bookingId) {
        Contract contract = contractRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng của booking #" + bookingId));

        if (contract.getStatus() != Contract.Status.ADMIN_SIGNED) {
            throw new RuntimeException("Hợp đồng chưa được quản trị viên ký duyệt.");
        }

        Renter renter = contract.getBooking().getRenter();

        // 🔢 Tạo mã OTP ngẫu nhiên
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // 💾 Lưu OTP
        OtpVerification otp = OtpVerification.builder()
                .contract(contract)
                .otpCode(otpCode)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .status(OtpVerification.Status.PENDING)
                .attemptCount(0)
                .build();
        otpVerificationRepository.save(otp);

        // ✉️ Gửi email OTP
        sendEmail(renter.getEmail(),
                "🔐 Mã OTP ký hợp đồng",
                """
                Xin chào %s,

                Mã OTP để ký hợp đồng #%d của bạn là: %s
                Mã này có hiệu lực trong 5 phút.

                Vui lòng đọc lại mã này cho nhân viên tại trạm để hoàn tất ký kết hợp đồng.

                Trân trọng,
                EV Rental System
                """.formatted(renter.getFullName(), contract.getContractId(), otpCode));
    }

    @Override
    @Transactional
    public void verifyRenterSignature(Long bookingId, String otpCode) {
        // 🔍 Lấy hợp đồng theo booking
        Contract contract = contractRepository.findByBooking_BookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng của booking #" + bookingId));

        // 🛑 Kiểm tra hợp đồng đã được admin ký chưa
        if (contract.getStatus() != Contract.Status.ADMIN_SIGNED) {
            throw new RuntimeException("Hợp đồng chưa được quản trị viên ký duyệt. Bạn không thể ký lúc này.");
        }

        // 🔍 Lấy OTP mới nhất của hợp đồng
        OtpVerification otp = otpVerificationRepository
                .findTopByContractOrderByCreatedAtDesc(contract)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã OTP."));

        // ⏰ Hết hạn
        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            otp.setStatus(OtpVerification.Status.FAILED);
            otpVerificationRepository.save(otp);
            throw new RuntimeException("Mã OTP đã hết hạn.");
        }

        // ❌ Sai mã
        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpVerificationRepository.save(otp);
            throw new RuntimeException("Mã OTP không đúng.");
        }

        // ✅ OTP hợp lệ
        otp.setVerifiedAt(LocalDateTime.now());
        otp.setStatus(OtpVerification.Status.VERIFIED);
        otpVerificationRepository.save(otp);

        // ✍️ Cập nhật hợp đồng (renter ký)
        contract.setStatus(Contract.Status.FULLY_SIGNED);
        contract.setRenterSignedAt(LocalDateTime.now());

        // 🔄 Regenerate lại file PDF có chữ ký renter
        String newFileUrl = pdfGeneratorService.generateContractFile(contract);
        contract.setContractFileUrl(newFileUrl);

        contractRepository.save(contract);

        // 📧 Gửi email thông báo cho renter
        Renter renter = contract.getBooking().getRenter();
        sendEmail(
                renter.getEmail(),
                "✅ Hợp đồng đã được ký thành công",
                """
                Xin chào %s,
                
                Bạn đã hoàn tất ký hợp đồng #%d thành công.
                Xe của bạn hiện đã sẵn sàng để nhận tại trạm.
                
                Trân trọng,
                EV Rental System
                """.formatted(renter.getFullName(), contract.getContractId())
        );
    }

    @Override
    public String uploadContractFile(File file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                    "folder", "ev_rental/contracts",
                    "resource_type", "raw" // raw dùng cho PDF, DOCX, ZIP,...
            ));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload hợp đồng lên Cloudinary: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public String getContractFileUrl(Long contractId, Long userId, String role) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng."));

        Booking booking = contract.getBooking();

        // 🧱 Kiểm tra phân quyền
        switch (role.toUpperCase()) {
            case "RENTER" -> {
                if (!booking.getRenter().getRenterId().equals(userId)) {
                    throw new RuntimeException("Bạn không có quyền xem hợp đồng này.");
                }
            }
            case "STAFF" -> {
                if (booking.getStaff() == null ||
                        !booking.getStaff().getStaffId().equals(userId)) {
                    throw new RuntimeException("Bạn không có quyền xem hợp đồng này.");
                }
            }
            case "ADMIN" -> {
                // Admin có quyền xem tất cả
            }
            default -> throw new RuntimeException("Vai trò người dùng không hợp lệ.");
        }

        // ✅ Nếu qua được kiểm tra => trả link Cloudinary
        return contract.getContractFileUrl();
    }

    // 📧 Gửi email helper
    private void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }

    private ContractResponseDTO mapToResponse(Contract contract) {
        return ContractResponseDTO.builder()
                .contractId(contract.getContractId())
                .bookingId(contract.getBooking().getBookingId())
                .contractType(contract.getContractType().name())
                .status(contract.getStatus().name())
                .contractDate(contract.getContractDate())
                .contractFileUrl(contract.getContractFileUrl())
                .terms(
                        termConditionRepository.findByContract(contract)
                                .stream()
                                .map(t -> new ContractResponseDTO.TermConditionDTO(
                                        t.getTermNumber(),
                                        t.getTermTitle(),
                                        t.getTermContent()
                                ))
                                .toList()
                )
                .build();
    }
}
