package com.example.ev_rental_backend.service.invoice;

import com.example.ev_rental_backend.dto.invoice.*;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceDetailRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import com.example.ev_rental_backend.repository.PriceListRepository;
import com.example.ev_rental_backend.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final PriceListRepository priceListRepository;
    private final PolicyService policyService;

    /**
     * Lấy chi tiết hóa đơn
     */
    public InvoiceResponseDto getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));
        return mapToResponseDto(invoice);
    }

    /**
     * Lấy danh sách hóa đơn của booking
     */
    public List<InvoiceResponseDto> getInvoicesByBookingId(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        return booking.getInvoices().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Tạo hóa đơn đặt cọc (BR-06, BR-23)
     */
    @Override
    public InvoiceResponseDto createDepositInvoice(Long bookingId) {
        // ✅ Lấy email renter hiện tại từ token trong SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String renterEmail = authentication.getName(); // do JwtAuthFilter đã set email ở đây

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy booking với ID: " + bookingId));

        // ✅ Kiểm tra booking có thuộc renter hiện tại không
        if (!booking.getRenter().getEmail().equalsIgnoreCase(renterEmail)) {
            throw new CustomException("Bạn không có quyền tạo hóa đơn cho booking này", HttpStatus.FORBIDDEN);
        }

        // ✅ (Phần còn lại giữ nguyên)
        if (booking.getStatus() != Booking.Status.PENDING &&
                booking.getStatus() != Booking.Status.RESERVED) {
            throw new CustomException("Chỉ có thể tạo hóa đơn đặt cọc cho booking ở trạng thái PENDING hoặc RESERVED",
                    HttpStatus.BAD_REQUEST);
        }

        boolean hasDepositInvoice = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.DEPOSIT);

        if (hasDepositInvoice) {
            throw new CustomException("Booking này đã có hóa đơn đặt cọc", HttpStatus.BAD_REQUEST);
        }

        double depositAmount = policyService.getPolicyValue(Policy.PolicyType.DEPOSIT_AMOUNT);

        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.DEPOSIT)
                .depositAmount(depositAmount)
                .totalAmount(depositAmount)
                .status(Invoice.Status.UNPAID)
                .paymentMethod(Invoice.PaymentMethod.MOMO)
                .notes("Deposit invoice automatically generated based on active policy.")
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponseDto(savedInvoice);
    }



    /**
     * Tạo hóa đơn cuối (BR-27)
     */
    @Override
    public InvoiceResponseDto createFinalInvoice(Long bookingId, CreateFinalInvoiceDto requestDto) {
        // 🔍 Tìm booking theo ID
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt xe có ID: " + bookingId));

        // ✅ Chỉ được tạo hóa đơn cuối khi booking đã hoàn tất
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new CustomException("Chỉ có thể tạo hóa đơn cuối khi đơn đặt xe đã hoàn tất",
                    HttpStatus.BAD_REQUEST);
        }

        // ✅ Kiểm tra xem booking này đã có hóa đơn cuối chưa
        boolean hasFinalInvoice = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.FINAL);
        if (hasFinalInvoice) {
            throw new CustomException("Đơn đặt xe này đã có hóa đơn cuối",
                    HttpStatus.BAD_REQUEST);
        }

        // ✅ Lấy số tiền cọc đã thanh toán (nếu có)
        Double depositAmount = booking.getInvoices().stream()
                .filter(inv -> inv.getType() == Invoice.Type.DEPOSIT && inv.getStatus() == Invoice.Status.PAID)
                .findFirst()
                .map(Invoice::getDepositAmount)
                .orElse(0.0);

        // ✅ Lấy giá trị threshold (ngưỡng tính theo giờ) từ bảng Policy, ví dụ: 4 giờ
        double thresholdHours = policyService.getPolicyValue(Policy.PolicyType.RENTAL_TIME_THRESHOLD_HOURS);

        // ✅ Tính thời gian thuê xe thực tế (tính theo giờ)
        long durationInHours = ChronoUnit.HOURS.between(
                booking.getStartDateTime(),
                booking.getActualReturnTime()
        );

        double totalAmount;
        if (durationInHours < thresholdHours) {
            // 🚗 Nếu thời gian thuê nhỏ hơn ngưỡng -> tính tiền theo giờ
            totalAmount = durationInHours * booking.getPriceSnapshotPerHour();
        } else {
            // 🕐 Nếu thời gian thuê vượt ngưỡng -> tính tiền theo ngày (làm tròn lên)
            long days = (long) Math.ceil(durationInHours / 24.0);
            totalAmount = days * booking.getPriceSnapshotPerDay();
        }

        // ✅ Tính thêm chi phí hư hại (nếu có)
        double damageCost = getDamageCost(booking);

        // ✅ Cập nhật tổng tiền sau khi cộng chi phí hư hại
        totalAmount += damageCost;

        // ✅ Tạo hóa đơn cuối cùng (FINAL)
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.FINAL)
                .depositAmount(depositAmount)
                .totalAmount(totalAmount)
                .status(Invoice.Status.UNPAID)
                .paymentMethod(Invoice.PaymentMethod.CASH)
                .notes("Hóa đơn cuối bao gồm tiền thuê và chi phí hư hại (nếu có)")
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // ✅ Cập nhật tổng tiền cho booking
        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking);

        return mapToResponseDto(savedInvoice);
    }

    private static double getDamageCost(Booking booking) {
        double damageCost = 0.0;

        // Duyệt qua tất cả các hóa đơn thuộc booking này
        for (Invoice inv : booking.getInvoices()) {
            if (inv.getInvoiceDetails() != null) {
                for (InvoiceDetail detail : inv.getInvoiceDetails()) {
                    // ✅ Cộng những dòng chi tiết có tổng tiền hợp lệ (lineTotal > 0)
                    if (detail.getLineTotal() != null && detail.getLineTotal() > 0) {
                        damageCost += detail.getLineTotal();
                    }
                }
            }
        }
        return damageCost;
    }


    /**
     * Thêm dòng chi phí (phụ tùng, phạt) (BR-13)
     */
    public InvoiceDetailResponseDto addInvoiceDetail(Long invoiceId, CreateInvoiceDetailDto requestDto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        // Kiểm tra invoice chưa được thanh toán
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Cannot add details to paid invoice",
                    HttpStatus.BAD_REQUEST);
        }

        PriceList priceList = null;
        if (requestDto.getPriceListId() != null) {
            priceList = priceListRepository.findById(requestDto.getPriceListId())
                    .orElseThrow(() -> new NotFoundException("Price list not found"));
        }

        // Tạo invoice detail
        InvoiceDetail detail = InvoiceDetail.builder()
                .invoice(invoice)
                .type(requestDto.getType())
                .priceList(priceList)
                .description(requestDto.getDescription())
                .quantity(requestDto.getQuantity())
                .unitPrice(requestDto.getUnitPrice())
                .build();

        InvoiceDetail savedDetail = invoiceDetailRepository.save(detail);

        // Cập nhật tổng tiền invoice
        updateInvoiceTotalAmount(invoice);

        return mapToDetailResponseDto(savedDetail);
    }

    /**
     * Xóa dòng chi phí
     */
    public void deleteInvoiceDetail(Long invoiceId, Long detailId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        InvoiceDetail detail = invoiceDetailRepository.findById(detailId)
                .orElseThrow(() -> new NotFoundException("Invoice detail not found with id: " + detailId));

        // Kiểm tra detail thuộc về invoice
        if (!detail.getInvoice().getInvoiceId().equals(invoiceId)) {
            throw new CustomException("Invoice detail does not belong to this invoice",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra invoice chưa được thanh toán
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Cannot delete details from paid invoice",
                    HttpStatus.BAD_REQUEST);
        }

        invoiceDetailRepository.delete(detail);

        // Cập nhật tổng tiền invoice
        updateInvoiceTotalAmount(invoice);
    }

    @Override
    @Transactional
    public List<InvoiceDetailResponseDto> addInvoiceDetailsFromPriceList(Long invoiceId, AddInvoiceDetailsRequest dto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn #" + invoiceId));

        List<InvoiceDetail> createdDetails = new ArrayList<>();

        for (Long priceId : dto.getPriceListIds()) {
            PriceList price = priceListRepository.findById(priceId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy phụ tùng #" + priceId));

            InvoiceDetail detail = InvoiceDetail.builder()
                    .invoice(invoice)
                    .priceList(price)
                    .type(InvoiceDetail.LineType.SPAREPART)
                    .description("Đền bù phụ tùng: " + price.getItemName())
                    .quantity(1)
                    .unitPrice(price.getUnitPrice())
                    .lineTotal(price.getUnitPrice())
                    .build();

            createdDetails.add(detail);
        }

        invoiceDetailRepository.saveAll(createdDetails);

        // Sau khi thêm detail, tự động tính lại tổng invoice
        recalculateInvoice(invoiceId);

        return createdDetails.stream()
                .map(detail -> InvoiceDetailResponseDto.builder()
                        .invoiceDetailId(detail.getInvoiceDetailId())
                        .itemName(detail.getPriceList().getItemName())
                        .description(detail.getDescription())
                        .unitPrice(detail.getUnitPrice())
                        .quantity(detail.getQuantity())
                        .lineTotal(detail.getLineTotal())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public InvoiceResponseDto recalculateInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hóa đơn #" + invoiceId));

        Booking booking = invoice.getBooking();

        // Tính chi phí thuê xe
        double thresholdHours = policyService.getPolicyValue(Policy.PolicyType.RENTAL_TIME_THRESHOLD_HOURS);
        long durationInHours = ChronoUnit.HOURS.between(
                booking.getStartDateTime(), booking.getActualReturnTime()
        );

        double rentalCost = (durationInHours < thresholdHours)
                ? durationInHours * booking.getPriceSnapshotPerHour()
                : Math.ceil(durationInHours / 24.0) * booking.getPriceSnapshotPerDay();

        // Cộng chi phí hư hại từ InvoiceDetail
        double damageCost = invoiceDetailRepository
                .findByInvoice_InvoiceId(invoiceId)
                .stream()
                .mapToDouble(detail -> detail.getLineTotal() != null ? detail.getLineTotal() : 0.0)
                .sum();

        // Tổng tiền mới
        double newTotal = rentalCost + damageCost;

        invoice.setTotalAmount(newTotal);
        invoiceRepository.save(invoice);

        // Cập nhật Booking tổng tiền
        booking.setTotalAmount(newTotal);
        bookingRepository.save(booking);

        return mapToResponseDto(invoice);
    }


    // Helper methods

    private void updateInvoiceTotalAmount(Invoice invoice) {
        if (invoice.getLines() == null) {
            invoice.setLines(new ArrayList<>());
        }

        Double detailsTotal = invoice.getLines().stream()
                .mapToDouble(detail -> detail.getLineTotal() != null ? detail.getLineTotal() : 0.0)
                .sum();

        invoice.setTotalAmount(detailsTotal);
        invoiceRepository.save(invoice);
    }

    /**
     * Tính tổng số tiền đã thanh toán
     */
    private Double calculateTotalPaid(Invoice invoice) {
        if (invoice.getTransactions() == null) {
            return 0.0;
        }

        return invoice.getTransactions().stream()
                .filter(t -> t.getStatus() == PaymentTransaction.Status.SUCCESS)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }

    private InvoiceResponseDto mapToResponseDto(Invoice invoice) {
        // Tính tổng số tiền đã thanh toán
        Double totalPaid = calculateTotalPaid(invoice);

        // Tính số tiền còn phải trả
        Double amountRemaining = invoice.getTotalAmount() - totalPaid;

        // Đảm bảo không âm
        if (amountRemaining < 0) {
            amountRemaining = 0.0;
        }

        return InvoiceResponseDto.builder()
                .invoiceId(invoice.getInvoiceId())
                .bookingId(invoice.getBooking().getBookingId())
                .type(invoice.getType())
                .depositAmount(invoice.getDepositAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountRemaining(amountRemaining) // ← Thêm field mới
                .status(invoice.getStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .completedAt(invoice.getCompletedAt())
                .details(invoice.getLines() != null ?
                        invoice.getLines().stream()
                                .map(this::mapToDetailResponseDto)
                                .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }

    private InvoiceDetailResponseDto mapToDetailResponseDto(InvoiceDetail detail) {
        return InvoiceDetailResponseDto.builder()
                .invoiceDetailId(detail.getInvoiceDetailId())
                .type(detail.getType())
                .priceListId(detail.getPriceList() != null ? detail.getPriceList().getPriceId() : null)
                .itemName(detail.getPriceList() != null ? detail.getPriceList().getItemName() : null)
                .description(detail.getDescription())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .lineTotal(detail.getLineTotal())
                .build();
    }
}
