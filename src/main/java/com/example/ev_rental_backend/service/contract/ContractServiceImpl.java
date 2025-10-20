package com.example.ev_rental_backend.service.contract;

import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.ContractRepository;
import com.example.ev_rental_backend.repository.TermConditionRepository;
import com.example.ev_rental_backend.service.notification.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService{

    private final BookingRepository bookingRepository;
    private final ContractRepository contractRepository;
    private final TermConditionRepository termConditionRepository;
    private final NotificationService notificationService;
    private final PdfGeneratorService pdfGeneratorService;

    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        if (booking.getStatus() != Booking.Status.RESERVED)
            throw new RuntimeException("Booking không hợp lệ để tạo hợp đồng");

        Contract contract = Contract.builder()
                .booking(booking)
                .contractType(Contract.ContractType.valueOf(dto.getContractType().toUpperCase()))
                .contractDate(LocalDateTime.now())
                .status(Contract.Status.PENDING_ADMIN_SIGNATURE)
                .build();

        // 🧩 1️⃣ Lưu contract trước để có ID
        contractRepository.save(contract);

        // 🧾 2️⃣ Lưu điều khoản
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

        // 📄 3️⃣ Render file HTML hoặc PDF
        String fileUrl = pdfGeneratorService.generateContractFile(contract); // → service riêng

        // 💾 4️⃣ Lưu URL vào DB
        contract.setContractFileUrl(fileUrl);
        contractRepository.save(contract);

        booking.setContract(contract);
        bookingRepository.save(booking);

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

        // 🧭 Giả định hiện tại có 1 Admin toàn cục
        Long adminId = 1L;

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
