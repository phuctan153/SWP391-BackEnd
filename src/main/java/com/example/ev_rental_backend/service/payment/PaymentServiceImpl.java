package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.invoice.InvoiceSummaryDTO;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.PaymentInitRequestDTO;
import com.example.ev_rental_backend.dto.payment.PaymentResponseDTO;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.mapper.PaymentMapper;
import com.example.ev_rental_backend.repository.BookingRepository;
import com.example.ev_rental_backend.repository.InvoiceRepository;
import com.example.ev_rental_backend.repository.PaymentTransactionRepository;
import com.example.ev_rental_backend.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

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
    public PaymentResponseDTO initPayment(PaymentInitRequestDTO requestDTO) {
        // 1. Validate invoice
//        Invoice invoice = invoiceRepository.findById(requestDTO.getInvoiceId())
//                .orElseThrow(() -> new ChangeSetPersister.NotFoundException(
//                        "Không tìm thấy invoice với ID: " + requestDTO.getInvoiceId()
//                ));

        Invoice invoice = invoiceRepository.findById(requestDTO.getInvoiceId()).orElseThrow();

//        // 2. Kiểm tra invoice đã thanh toán chưa
//        if (invoice.getStatus() == Invoice.Status.COMPLETED) {
//            throw new CustomException("Invoice đã được thanh toán");
//        }
//
//        if (invoice.getStatus() == Invoice.Status.CANCELLED) {
//            throw new CustomException("Invoice đã bị hủy");
//        }

        // 3. Kiểm tra đã có transaction thành công chưa
        boolean hasSuccessTransaction = paymentTransactionRepository
                .existsByInvoice_InvoiceIdAndStatus(
                        invoice.getInvoiceId(),
                        PaymentTransaction.Status.SUCCESS
                );

//        if (hasSuccessTransaction) {
//            throw new CustomException("Invoice đã có giao dịch thanh toán thành công");
//        }

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
                CreateMomoResponse momoResponse = momoService.createMomo(
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
//                throw new CustomException("Không thể khởi tạo thanh toán Momo: " + e.getMessage());
            }
        }

        return response;
    }

    @Override
    public InvoiceSummaryDTO getInvoiceSummary(Long invoiceId) {
        return null;
    }

    @Override
    public void handlePaymentSuccess(Long invoiceId, String transactionId, Double amount) {

    }

    @Override
    public void handlePaymentFailed(Long invoiceId, String reason) {

    }
}
