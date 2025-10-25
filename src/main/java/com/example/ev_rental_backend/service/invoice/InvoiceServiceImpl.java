package com.example.ev_rental_backend.service.invoice;

import com.example.ev_rental_backend.dto.invoice.*;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceDetailRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import com.example.ev_rental_backend.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    public InvoiceResponseDto createDepositInvoice(Long bookingId, CreateDepositInvoiceDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Kiểm tra booking phải ở trạng thái RESERVED
        if (booking.getStatus() != Booking.Status.RESERVED) {
            throw new CustomException("Can only create deposit invoice for RESERVED booking",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra xem đã có deposit invoice chưa
        boolean hasDepositInvoice = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.DEPOSIT);

        if (hasDepositInvoice) {
            throw new CustomException("Deposit invoice already exists for this booking",
                    HttpStatus.BAD_REQUEST);
        }

        // Tạo deposit invoice
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.DEPOSIT)
                .depositAmount(requestDto.getDepositAmount())
                .totalAmount(requestDto.getDepositAmount())
                .status(Invoice.Status.UNPAID)
                .paymentMethod(Invoice.PaymentMethod.MOMO)
                .notes(requestDto.getNotes())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponseDto(savedInvoice);
    }

    /**
     * Tạo hóa đơn cuối (BR-27)
     */
    public InvoiceResponseDto createFinalInvoice(Long bookingId, CreateFinalInvoiceDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Kiểm tra booking phải ở trạng thái COMPLETED
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new CustomException("Can only create final invoice for COMPLETED booking",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra xem đã có final invoice chưa
        boolean hasFinalInvoice = booking.getInvoices().stream()
                .anyMatch(inv -> inv.getType() == Invoice.Type.FINAL);

        if (hasFinalInvoice) {
            throw new CustomException("Final invoice already exists for this booking",
                    HttpStatus.BAD_REQUEST);
        }

        // Lấy số tiền đã đặt cọc
        Double depositAmount = booking.getInvoices().stream()
                .filter(inv -> inv.getType() == Invoice.Type.DEPOSIT
                        && inv.getStatus() == Invoice.Status.PAID)
                .findFirst()
                .map(Invoice::getDepositAmount)
                .orElse(0.0);

        // Tạo final invoice
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.FINAL)
                .depositAmount(depositAmount)
                .totalAmount(requestDto.getTotalAmount())
                .status(Invoice.Status.UNPAID)
                .paymentMethod(Invoice.PaymentMethod.CASH)
                .notes(requestDto.getNotes())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return mapToResponseDto(savedInvoice);
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
