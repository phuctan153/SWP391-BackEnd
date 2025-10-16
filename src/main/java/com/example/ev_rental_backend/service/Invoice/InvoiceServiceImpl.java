package com.example.ev_rental_backend.service.Invoice;

import com.example.ev_rental_backend.dto.invoice.InvoiceCreateRequestDTO;
import com.example.ev_rental_backend.dto.invoice.InvoiceResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.entity.InvoiceDetail;
import com.example.ev_rental_backend.entity.SparePart;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.mapper.InvoiceMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceDetailRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import com.example.ev_rental_backend.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SparePartRepository sparePartRepository;
    private final InvoiceMapper invoiceMapper;

    /**
     * Lấy chi tiết invoice theo ID
     */
    @Override
    public InvoiceResponseDTO getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy invoice với ID: " + invoiceId
                ));

        return invoiceMapper.toDto(invoice);
    }

    /**
     * Lấy danh sách invoice của booking
     */
    @Override
    public List<InvoiceResponseDTO> getInvoicesByBookingId(Long bookingId) {
        // Kiểm tra booking tồn tại
        if (!bookingRepository.existsById(bookingId)) {
            throw new NotFoundException("Không tìm thấy booking với ID: " + bookingId);
        }

        List<Invoice> invoices = invoiceRepository.findByBooking_BookingId(bookingId);

        return invoices.stream()
                .map(invoice -> {
                    // Fetch chi tiết từng invoice
                    Invoice detailedInvoice = invoiceRepository
                            .findByIdWithDetails(invoice.getInvoiceId())
                            .orElse(invoice);
                    return invoiceMapper.toDto(detailedInvoice);
                })
                .collect(Collectors.toList());
    }

    /**
     * Tạo invoice Final (sau khi trả xe)
     */
    @Override
    @Transactional
    public InvoiceResponseDTO createFinalInvoice(InvoiceCreateRequestDTO requestDTO) {
        // 1. Validate booking
        Booking booking = bookingRepository.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy booking với ID: " + requestDTO.getBookingId()
                ));

        // 2. Kiểm tra booking đã trả xe chưa
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new CustomException(
                    "Chỉ có thể tạo invoice Final khi booking đã hoàn tất (status = COMPLETED)"
            );
        }

        // 3. Kiểm tra đã có invoice Final chưa
        boolean hasFinalInvoice = invoiceRepository.existsByBooking_BookingIdAndType(
                requestDTO.getBookingId(),
                Invoice.Type.Final
        );

        if (hasFinalInvoice) {
            throw new CustomException("Booking này đã có invoice Final");
        }

        // 4. Lấy invoice Deposit để tính deposit amount
        Invoice depositInvoice = invoiceRepository
                .findByBooking_BookingIdAndType(requestDTO.getBookingId(), Invoice.Type.Deposit)
                .orElse(null);

        Double depositAmount = (depositInvoice != null) ? depositInvoice.getTotalAmount() : 0.0;

        // 5. Tính tổng tiền từ line items
        Double totalAmount = 0.0;

        if (requestDTO.getLineItems() != null && !requestDTO.getLineItems().isEmpty()) {
            for (InvoiceCreateRequestDTO.InvoiceLineItemDTO item : requestDTO.getLineItems()) {
                Double lineTotal = item.getQuantity() * item.getUnitPrice();
                totalAmount += lineTotal;
            }
        }

        // Thêm chi phí thuê xe cơ bản
        Double rentalCost = booking.getTotalAmount();
        totalAmount += rentalCost;

        // Trừ deposit
        totalAmount -= depositAmount;

        // 6. Tạo Invoice entity
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .type(Invoice.Type.Final)
                .depositAmount(depositAmount)
                .totalAmount(totalAmount)
                .status(Invoice.Status.PENDING)
                .notes(requestDTO.getNotes())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 7. Tạo line items
        List<InvoiceDetail> lineItems = new ArrayList<>();

        // Line item cho chi phí thuê xe
        InvoiceDetail rentalLine = InvoiceDetail.builder()
                .invoice(savedInvoice)
                .type(InvoiceDetail.LineType.SERVICE)
                .description("Chi phí thuê xe")
                .quantity(1)
                .unitPrice(rentalCost)
                .lineTotal(rentalCost)
                .build();
        lineItems.add(rentalLine);

        // Các line items khác (spare parts, penalties)
        if (requestDTO.getLineItems() != null) {
            for (InvoiceCreateRequestDTO.InvoiceLineItemDTO itemDto : requestDTO.getLineItems()) {
                InvoiceDetail.LineType lineType = InvoiceDetail.LineType.valueOf(itemDto.getType());

                InvoiceDetail line = InvoiceDetail.builder()
                        .invoice(savedInvoice)
                        .type(lineType)
                        .description(itemDto.getDescription())
                        .quantity(itemDto.getQuantity())
                        .unitPrice(itemDto.getUnitPrice())
                        .build();

                // Nếu là SPAREPART, gán spare part
                if (lineType == InvoiceDetail.LineType.SPAREPART && itemDto.getSparepartId() != null) {
                    SparePart sparePart = sparePartRepository.findById(itemDto.getSparepartId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Không tìm thấy spare part với ID: " + itemDto.getSparepartId()
                            ));

                    // Kiểm tra tồn kho
                    if (sparePart.getStockQuantity() < itemDto.getQuantity()) {
                        throw new CustomException(
                                "Spare part '" + sparePart.getPartName() +
                                        "' không đủ tồn kho (còn " + sparePart.getStockQuantity() + ")"
                        );
                    }

                    line.setSparePart(sparePart);

                    // Trừ tồn kho
                    sparePart.setStockQuantity(sparePart.getStockQuantity() - itemDto.getQuantity());
                    sparePartRepository.save(sparePart);
                }

                lineItems.add(line);
            }
        }

        invoiceDetailRepository.saveAll(lineItems);

        log.info("Created Final invoice {} for booking {}", savedInvoice.getInvoiceId(), booking.getBookingId());

        // 8. Fetch lại invoice với đầy đủ thông tin
        Invoice invoiceWithDetails = invoiceRepository
                .findByIdWithDetails(savedInvoice.getInvoiceId())
                .orElse(savedInvoice);

        return invoiceMapper.toDto(invoiceWithDetails);
    }
}
