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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public InvoiceResponseDto createFinalInvoice(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn đặt xe có ID: " + bookingId));

        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new CustomException("Chỉ có thể tạo hóa đơn cuối khi đơn đặt xe đã hoàn tất",
                    HttpStatus.BAD_REQUEST);
        }

        boolean hasFinalInvoice = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.FINAL);
        if (hasFinalInvoice) {
            throw new CustomException("Đơn đặt xe này đã có hóa đơn cuối", HttpStatus.BAD_REQUEST);
        }

        Double depositAmount = booking.getInvoices().stream()
                .filter(inv -> inv.getType() == Invoice.Type.DEPOSIT && inv.getStatus() == Invoice.Status.PAID)
                .findFirst()
                .map(Invoice::getDepositAmount)
                .orElse(0.0);

        // ✅ Lấy ngưỡng 4 giờ (policy)
        double thresholdHours = policyService.getPolicyValue(Policy.PolicyType.RENTAL_TIME_THRESHOLD_HOURS);

        // 1️⃣ Tính tiền thuê chính (từ start -> end)
        long plannedHours = ChronoUnit.HOURS.between(
                booking.getStartDateTime(),
                booking.getEndDateTime()
        );

        double rentalCost;
        if (plannedHours < thresholdHours) {
            // Trường hợp cực hiếm — thuê rất ngắn (<4h)
            rentalCost = plannedHours * booking.getPriceSnapshotPerHour();
        } else {
            long plannedDays = (long) Math.ceil(plannedHours / 24.0);
            rentalCost = plannedDays * booking.getPriceSnapshotPerDay();
        }

        // 2️⃣ Tính phụ phí trễ (nếu có)
        long delayHours = ChronoUnit.HOURS.between(
                booking.getEndDateTime(),
                booking.getActualReturnTime()
        );
        delayHours = Math.max(delayHours, 0);

        double lateFee = 0;
        if (delayHours > 0) {
            if (delayHours <= thresholdHours) {
                // 🚗 Trễ <= 4h => tính thêm theo giờ
                lateFee = delayHours * booking.getPriceSnapshotPerHour();
            } else {
                // 🕐 Trễ > 4h => tính thêm theo ngày
                long extraDays = (long) Math.ceil(delayHours / 24.0);
                lateFee = extraDays * booking.getPriceSnapshotPerDay();
            }
        }

        // 3️⃣ Chi phí hư hại
        double damageCost = getDamageCost(booking);

        // 4️⃣ Tổng tiền cuối cùng
        double totalAmount = rentalCost + lateFee + damageCost;

        // 🧾 Tạo hóa đơn FINAL
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.FINAL)
                .depositAmount(depositAmount)
                .totalAmount(totalAmount)
                .status(Invoice.Status.UNPAID)
                .paymentMethod(Invoice.PaymentMethod.CASH)
                .notes("Bao gồm tiền thuê, phụ phí trễ và chi phí hư hại (nếu có)")
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

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

    // 7.2. Invoice Details
    /**
     * Thêm dòng chi phí (phụ tùng, phạt) (BR-13)
     */
    @Transactional
    public InvoiceDetailResponseDto addInvoiceDetail(Long invoiceId, CreateInvoiceDetailDto requestDto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        // Kiểm tra invoice chưa được thanh toán hoàn toàn
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Cannot add details to paid invoice",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra invoice phải là FINAL (không cho thêm chi phí vào invoice DEPOSIT)
        if (invoice.getType() != Invoice.Type.FINAL) {
            throw new CustomException("Can only add details to FINAL invoice",
                    HttpStatus.BAD_REQUEST);
        }

        PriceList priceList = null;
        if (requestDto.getPriceListId() != null) {
            priceList = priceListRepository.findById(requestDto.getPriceListId())
                    .orElseThrow(() -> new NotFoundException("Price list not found with id: " + requestDto.getPriceListId()));
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

        // Tính lineTotal sẽ được tự động tính trong @PrePersist
        InvoiceDetail savedDetail = invoiceDetailRepository.save(detail);

        // ✅ CỘNG THÊM vào tổng tiền invoice (không ghi đè)
        Double lineTotal = savedDetail.getLineTotal() != null ? savedDetail.getLineTotal() : 0.0;
        addAmountToInvoice(invoice, lineTotal);

        log.info("Added invoice detail: {} to invoice {}, line total: {}, new invoice total: {}",
                savedDetail.getInvoiceDetailId(), invoiceId, lineTotal, invoice.getTotalAmount());

        return mapToDetailResponseDto(savedDetail);
    }

    /**
     * Xóa dòng chi phí
     */
    @Transactional
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

        // Lưu lại lineTotal trước khi xóa
        Double lineTotal = detail.getLineTotal() != null ? detail.getLineTotal() : 0.0;

        // Xóa detail
        invoiceDetailRepository.delete(detail);

        // ✅ TRỪ số tiền này khỏi invoice total
        addAmountToInvoice(invoice, -lineTotal);

        log.info("Deleted invoice detail: {} from invoice {}, line total deducted: {}, new invoice total: {}",
                detailId, invoiceId, lineTotal, invoice.getTotalAmount());
    }

    /**
     * Cập nhật dòng chi phí
     */
    @Transactional
    public InvoiceDetailResponseDto updateInvoiceDetail(Long invoiceId, Long detailId,
                                                        UpdateInvoiceDetailDto requestDto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        InvoiceDetail detail = invoiceDetailRepository.findById(detailId)
                .orElseThrow(() -> new NotFoundException("Invoice detail not found with id: " + detailId));

        // Kiểm tra detail có thuộc invoice này không
        if (!detail.getInvoice().getInvoiceId().equals(invoiceId)) {
            throw new CustomException("Invoice detail does not belong to this invoice",
                    HttpStatus.BAD_REQUEST);
        }

        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Cannot update details of paid invoice",
                    HttpStatus.BAD_REQUEST);
        }

        // Lưu lại lineTotal cũ để tính chênh lệch
        Double oldLineTotal = detail.getLineTotal() != null ? detail.getLineTotal() : 0.0;

        // Cập nhật các field
        if (requestDto.getQuantity() != null) {
            detail.setQuantity(requestDto.getQuantity());
        }
        if (requestDto.getUnitPrice() != null) {
            detail.setUnitPrice(requestDto.getUnitPrice());
        }
        if (requestDto.getDescription() != null) {
            detail.setDescription(requestDto.getDescription());
        }

        // Tính lại lineTotal (sẽ được tự động tính trong @PreUpdate)
        InvoiceDetail updatedDetail = invoiceDetailRepository.save(detail);

        // ✅ Cập nhật tổng tiền invoice: trừ cũ, cộng mới
        Double newLineTotal = updatedDetail.getLineTotal() != null ? updatedDetail.getLineTotal() : 0.0;
        Double difference = newLineTotal - oldLineTotal;

        if (difference != 0) {
            addAmountToInvoice(invoice, difference);
        }

        log.info("Updated invoice detail {}: old line total={}, new line total={}, difference={}, new invoice total={}",
                detailId, oldLineTotal, newLineTotal, difference, invoice.getTotalAmount());

        return mapToDetailResponseDto(updatedDetail);
    }

    // ==================== HELPER METHODS ====================

    /**
     * ✅ Phương thức CỘNG/TRỪ tiền vào invoice
     * - Nếu amount > 0: cộng thêm
     * - Nếu amount < 0: trừ đi
     *
     * @param invoice Invoice cần cập nhật
     * @param amount Số tiền cần cộng/trừ
     */
    private void addAmountToInvoice(Invoice invoice, Double amount) {
        if (amount == null || amount == 0) {
            return;
        }

        Double currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
        Double newTotal = currentTotal + amount;

        // Không cho tổng tiền âm
        if (newTotal < 0) {
            throw new CustomException(
                    String.format("Invoice total amount cannot be negative. Current: %.2f, Amount to add: %.2f",
                            currentTotal, amount),
                    HttpStatus.BAD_REQUEST
            );
        }

        invoice.setTotalAmount(newTotal);
        invoiceRepository.save(invoice);

        log.debug("Invoice {} total amount updated: {:.2f} + {:.2f} = {:.2f}",
                invoice.getInvoiceId(), currentTotal, amount, newTotal);
    }

    /**
     * ✅ Tính lại TOÀN BỘ tổng tiền invoice từ đầu
     * Công thức: totalAmount = rentalAmount + extraCharges - depositAmount
     *
     * Sử dụng khi:
     * - Cần verify hoặc fix dữ liệu
     * - Sau khi import dữ liệu
     * - Admin muốn recalculate
     */
    @Transactional
    public void recalculateInvoiceTotalAmount(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        Booking booking = invoice.getBooking();
        if (booking == null) {
            throw new CustomException("Invoice has no associated booking", HttpStatus.BAD_REQUEST);
        }

        // Bước 1: Tính tiền thuê xe từ booking
        Double rentalAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;

        // Bước 2: Tính tổng các chi phí phụ từ invoice lines
        Double extraCharges = 0.0;
        if (invoice.getLines() != null && !invoice.getLines().isEmpty()) {
            extraCharges = invoice.getLines().stream()
                    .mapToDouble(detail -> detail.getLineTotal() != null ? detail.getLineTotal() : 0.0)
                    .sum();
        }

        // Bước 3: Tổng tiền = tiền thuê + chi phí phụ
        Double finalAmount = rentalAmount + extraCharges;

        // Không cho tổng tiền âm
        if (finalAmount < 0) {
            log.warn("Recalculated invoice {} would have negative total: rental={}, extras={}, deposit={}",
                    invoiceId, rentalAmount, extraCharges);
            finalAmount = 0.0;
        }

        invoice.setTotalAmount(finalAmount);
        invoiceRepository.save(invoice);

        log.info("Recalculated invoice {}: rental={}, extras={}, deposit={}, final total={}",
                invoiceId, rentalAmount, extraCharges, finalAmount);
    }

    /**
     * Lấy chi tiết phân tích tổng tiền invoice
     */
    public Map<String, Object> getInvoiceAmountBreakdown(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        Booking booking = invoice.getBooking();
        if (booking == null) {
            throw new CustomException("Invoice has no associated booking", HttpStatus.BAD_REQUEST);
        }

        Double rentalAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
        Double depositAmount = invoice.getDepositAmount() != null ? invoice.getDepositAmount() : 0.0;

        // Tính tổng chi phí phụ theo từng loại
        Map<String, Double> extraChargesByType = new HashMap<>();
        Double totalExtraCharges = 0.0;
        List<InvoiceDetailResponseDto> detailsList = new ArrayList<>();

        if (invoice.getLines() != null && !invoice.getLines().isEmpty()) {
            for (InvoiceDetail detail : invoice.getLines()) {
                Double lineTotal = detail.getLineTotal() != null ? detail.getLineTotal() : 0.0;
                totalExtraCharges += lineTotal;

                String typeName = detail.getType().name();
                extraChargesByType.merge(typeName, lineTotal, Double::sum);

                detailsList.add(mapToDetailResponseDto(detail));
            }
        }

        Double subtotal = rentalAmount + totalExtraCharges;

        Map<String, Object> extraChargesMap = new HashMap<>();
        extraChargesMap.put("total", totalExtraCharges);
        extraChargesMap.put("byType", extraChargesByType);
        extraChargesMap.put("details", detailsList);

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("invoiceId", invoiceId);
        breakdown.put("invoiceType", invoice.getType().name());
        breakdown.put("rentalAmount", rentalAmount);
        breakdown.put("depositAmount", depositAmount);
        breakdown.put("extraCharges", extraChargesMap);
        breakdown.put("finalAmount", subtotal);
        breakdown.put("currentInvoiceTotal", invoice.getTotalAmount());

        return breakdown;
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
