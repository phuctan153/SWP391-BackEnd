package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.invoice.InvoiceSummaryDTO;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.PaymentInitRequestDTO;
import com.example.ev_rental_backend.dto.payment.PaymentResponseDTO;
import com.example.ev_rental_backend.entity.Booking;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.Vehicle;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.mapper.PaymentMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import com.example.ev_rental_backend.repository.PaymentTransactionRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentMapper paymentMapper;
    private final MomoService momoService;

    @Override
    @Transactional
    public PaymentResponseDTO initPayment(PaymentInitRequestDTO requestDTO) {
        // 1. Validate invoice
        Invoice invoice = invoiceRepository.findById(requestDTO.getInvoiceId())
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy invoice với ID: " + requestDTO.getInvoiceId()
                ));

        // 2. Kiểm tra invoice đã thanh toán chưa
        if (invoice.getStatus() == Invoice.Status.COMPLETED) {
            throw new CustomException("Invoice đã được thanh toán");
        }

        if (invoice.getStatus() == Invoice.Status.CANCELLED) {
            throw new CustomException("Invoice đã bị hủy");
        }

        // 3. Kiểm tra đã có transaction thành công chưa
        boolean hasSuccessTransaction = paymentTransactionRepository
                .existsByInvoice_InvoiceIdAndStatus(
                        invoice.getInvoiceId(),
                        PaymentTransaction.Status.SUCCESS
                );

        if (hasSuccessTransaction) {
            throw new CustomException("Invoice đã có giao dịch thanh toán thành công");
        }

        // 4. Xác định payment method
        String paymentMethod = requestDTO.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "MOMO"; // Default
        }

        // 5. Tạo payment transaction (PENDING)
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .amount(invoice.getTotalAmount())
                .status(PaymentTransaction.Status.FAILED) // Tạm thời FAILED, đợi callback
                .transactionTime(LocalDateTime.now())
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        // 6. Gọi Momo API (nếu payment method là MOMO)
        PaymentResponseDTO response = paymentMapper.toPaymentResponseDTO(savedTransaction);
        response.setPaymentMethod(paymentMethod);

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            try {
                CreateMomoResponse momoResponse = momoService.createMomoPayment(
                        invoice.getBooking().getBookingId(),
                        invoice.getInvoiceId()
                );

                response.setPayUrl(momoResponse.getPayUrl());
                response.setQrCodeUrl(momoResponse.getQrCodeUrl());
                response.setDeeplink(momoResponse.getDeeplink());
                response.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Momo timeout 15 phút

                log.info("Created Momo payment for invoice {}: {}",
                        invoice.getInvoiceId(), momoResponse.getPayUrl());
            } catch (Exception e) {
                log.error("Error creating Momo payment: {}", e.getMessage(), e);
                throw new CustomException("Không thể khởi tạo thanh toán Momo: " + e.getMessage());
            }
        }

        return response;
    }

    @Override
    @Transactional
    public InvoiceSummaryDTO getInvoiceSummary(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException(
                        "Không tìm thấy invoice với ID: " + invoiceId
                ));

        return paymentMapper.toInvoiceSummaryDTO(invoice);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long invoiceId, String transactionId, Double amount) {
        try {
            // 1. Lấy invoice
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new NotFoundException("Invoice not found: " + invoiceId));

            // 2. Update invoice status
            invoice.setStatus(Invoice.Status.COMPLETED);
            invoice.setCompletedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            // 3. Update payment transaction
            PaymentTransaction transaction = paymentTransactionRepository
                    .findByInvoice_InvoiceId(invoiceId)
                    .stream()
                    .filter(t -> t.getStatus() == PaymentTransaction.Status.FAILED)
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                transaction.setStatus(PaymentTransaction.Status.SUCCESS);
                transaction.setTransactionTime(LocalDateTime.now());
                paymentTransactionRepository.save(transaction);
            }

            // 4. Update booking status dựa trên invoice type
            Booking booking = invoice.getBooking();

            if (invoice.getType() == Invoice.Type.Deposit) {
                // Deposit thành công → Booking RESERVED
                booking.setStatus(Booking.Status.RESERVED);
                booking.setDepositStatus(Booking.DepositStatus.PAID);

                // Đổi vehicle status sang RESERVED
                Vehicle vehicle = booking.getVehicle();
                vehicle.setStatus(Vehicle.Status.RESERVED);
                vehicleRepository.save(vehicle);

                log.info("✅ Deposit paid for booking {}", booking.getBookingId());
            } else if (invoice.getType() == Invoice.Type.Final) {
                // Final payment thành công → Booking COMPLETED
                booking.setStatus(Booking.Status.COMPLETED);

                // Đổi vehicle status sang AVAILABLE
                Vehicle vehicle = booking.getVehicle();
                vehicle.setStatus(Vehicle.Status.AVAILABLE);
                vehicleRepository.save(vehicle);

                log.info("✅ Final payment completed for booking {}", booking.getBookingId());
            }

            bookingRepository.save(booking);

            log.info("Payment success processed for invoice {}", invoiceId);
        } catch (Exception e) {
            log.error("Error handling payment success: {}", e.getMessage(), e);
            throw new CustomException("Lỗi xử lý thanh toán thành công: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePaymentFailed(Long invoiceId, String reason) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElse(null);

            if (invoice != null) {
                invoice.setNotes("Thanh toán thất bại: " + reason);
                invoiceRepository.save(invoice);

                log.warn("Payment failed for invoice {}: {}", invoiceId, reason);
            }
        } catch (Exception e) {
            log.error("Error handling payment failed: {}", e.getMessage(), e);
        }
    }
}
