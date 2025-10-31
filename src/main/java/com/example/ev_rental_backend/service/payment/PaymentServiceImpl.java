package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.payment.*;
import com.example.ev_rental_backend.dto.payos.PayOSPaymentInfoDto;
import com.example.ev_rental_backend.dto.payos.PayOSWebhookRequest;
import com.example.ev_rental_backend.dto.payos.PayOSWebhookResponse;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.*;
import com.example.ev_rental_backend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final WalletRepository walletRepository;
    private final RenterRepository renterRepository;
    private final MomoPaymentService momoPaymentService;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final PayOSPaymentService payosPaymentService;

    /**
     * Thanh toán bằng tiền mặt (Staff xác nhận)
     */
    public PaymentResponseDto payByCash(Long invoiceId, PaymentRequestDto requestDto) {
        Invoice invoice = getInvoiceAndValidate(invoiceId, requestDto.getAmount());

        // Tạo transaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .amount(BigDecimal.valueOf(requestDto.getAmount()))
                .status(PaymentTransaction.Status.SUCCESS)
                .transactionType(PaymentTransaction.TransactionType.INVOICE_CASH)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        // Cập nhật trạng thái invoice
        updateInvoiceStatus(invoice, requestDto.getAmount());

        return mapToPaymentResponseDto(savedTransaction);
    }

    /**
     * Thanh toán bằng ví (BR-30)
     */
    public PaymentResponseDto payByWallet(Long invoiceId, PaymentRequestDto requestDto) {
        Invoice invoice = getInvoiceAndValidate(invoiceId, requestDto.getAmount());

        // Lấy renter từ booking
        Renter renter = invoice.getBooking().getRenter();

        // Lấy wallet
        Wallet wallet = walletRepository.findByRenter(renter)
                .orElseThrow(() -> new NotFoundException("Wallet not found for renter"));

        // Kiểm tra ví có active không
        if (wallet.getStatus() != Wallet.Status.ACTIVE) {
            throw new CustomException("Wallet is not active", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(BigDecimal.valueOf(requestDto.getAmount())) < 0) {
            throw new CustomException("Insufficient wallet balance", HttpStatus.BAD_REQUEST);
        }

        // Trừ tiền trong ví
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(requestDto.getAmount())));
        walletRepository.save(wallet);

        // Tạo transaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .wallet(wallet)
                .amount(BigDecimal.valueOf(requestDto.getAmount()))
                .status(PaymentTransaction.Status.SUCCESS)
                .transactionType(PaymentTransaction.TransactionType.INVOICE_WALLET)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        // Cập nhật trạng thái invoice
        updateInvoiceStatus(invoice, requestDto.getAmount());

        return mapToPaymentResponseDto(savedTransaction);
    }

    /**
     * Tạo payment MoMo cho invoice (BR-30)
     */
    @Transactional
    public MomoPaymentInfoDto createMomoPayment(Long invoiceId, PaymentRequestDto requestDto) {
        // 1. Validate invoice
        Invoice invoice = getInvoiceAndValidate(invoiceId, requestDto.getAmount());

        // 2. Tính số tiền còn phải trả
        Double totalPaid = getTotalPaid(invoice);
        Double amountRemaining = invoice.getTotalAmount() - totalPaid;

        if (requestDto.getAmount() > amountRemaining) {
            throw new CustomException(
                    String.format("Payment amount %.2f exceeds remaining amount %.2f",
                            requestDto.getAmount(), amountRemaining),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Tạo payment transaction (PENDING)
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .amount(BigDecimal.valueOf(requestDto.getAmount()))
                .status(PaymentTransaction.Status.PENDING)
                .transactionType(PaymentTransaction.TransactionType.INVOICE_MOMO)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        log.info("Created payment transaction {} for invoice {}",
                savedTransaction.getTransactionId(), invoiceId);

        // 4. Tạo payment với MoMo
        String orderInfo = String.format(
                "Thanh toan hoa don #%d - EV Station",
                invoiceId
        );

        MomoPaymentInfoDto momoPayment = momoPaymentService.createPayment(
                savedTransaction.getTransactionId(),
                requestDto.getAmount().longValue(),
                orderInfo
        );

        // 5. Kiểm tra kết quả
        if (momoPayment.getResultCode() != 0) {
            // Tạo payment thất bại
            savedTransaction.setStatus(PaymentTransaction.Status.FAILED);
            paymentTransactionRepository.save(savedTransaction);

            log.error("MoMo payment creation failed for transaction {}",
                    savedTransaction.getTransactionId());
        }

        return momoPayment;
    }

    /**
     * Xử lý IPN từ MoMo
     *
     * IPN = Instant Payment Notification
     * MoMo gọi về backend để thông báo kết quả thanh toán
     */
    @Transactional
    public MomoIPNResponse handleMomoIPN(MomoIPNRequest ipnRequest) {

        log.info("Processing MoMo IPN - OrderId: {}, ResultCode: {}, TransId: {}",
                ipnRequest.getOrderId(), ipnRequest.getResultCode(), ipnRequest.getTransId());

        try {
            // 1. Verify signature
            boolean isValidSignature = momoPaymentService.verifyIPNSignature(ipnRequest);

            if (!isValidSignature) {
                log.error("Invalid MoMo IPN signature - OrderId: {}", ipnRequest.getOrderId());
                return momoPaymentService.createIPNResponse(ipnRequest, false);
            }

            // 2. Parse transaction ID từ order ID
            Long transactionId = momoPaymentService.parseTransactionIdFromOrderId(
                    ipnRequest.getOrderId());

            // 3. Lấy transaction
            PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                    .orElseThrow(() -> new NotFoundException("Transaction not found: " + transactionId));

            // 4. Kiểm tra transaction chưa xử lý (tránh duplicate IPN)
            if (transaction.getStatus() != PaymentTransaction.Status.PENDING) {
                log.warn("Transaction {} already processed, status: {}",
                        transactionId, transaction.getStatus());
                return momoPaymentService.createIPNResponse(ipnRequest, true);
            }

            // 5. Xử lý theo result code
            if (ipnRequest.getResultCode() == 0) {
                // Thanh toán thành công
                handleSuccessfulPayment(transaction, ipnRequest);
            } else {
                // Thanh toán thất bại
                handleFailedPayment(transaction, ipnRequest);
            }

            // 6. Trả về response cho MoMo
            return momoPaymentService.createIPNResponse(ipnRequest, true);

        } catch (Exception e) {
            log.error("Error processing MoMo IPN - OrderId: {}", ipnRequest.getOrderId(), e);
            return momoPaymentService.createIPNResponse(ipnRequest, false);
        }
    }

    /**
     * Thử lại giao dịch thất bại (BR-29)
     */
    public PaymentResponseDto retryPayment(Long invoiceId, RetryPaymentRequestDto requestDto) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        // Lấy transaction thất bại
        PaymentTransaction failedTransaction = paymentTransactionRepository
                .findById(requestDto.getTransactionId())
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        // Kiểm tra transaction có thuộc invoice không
        if (!failedTransaction.getInvoice().getInvoiceId().equals(invoiceId)) {
            throw new CustomException("Transaction does not belong to this invoice",
                    HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra transaction có failed không
        if (failedTransaction.getStatus() != PaymentTransaction.Status.FAILED) {
            throw new CustomException("Can only retry failed transactions",
                    HttpStatus.BAD_REQUEST);
        }

        // Retry dựa trên loại payment
        switch (failedTransaction.getTransactionType()) {
            case INVOICE_WALLET:
                return payByWallet(invoiceId, PaymentRequestDto.builder()
                        .amount(failedTransaction.getAmount().doubleValue())
                        .build());
            case INVOICE_MOMO:
                // Với MoMo cần tạo payment request mới
                throw new CustomException("Please create a new MoMo payment",
                        HttpStatus.BAD_REQUEST);
            default:
                throw new CustomException("Cannot retry this transaction type",
                        HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy chi tiết giao dịch
     */
    public TransactionResponseDto getTransactionById(Long transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found with id: " + transactionId));

        return mapToTransactionResponseDto(transaction);
    }

    /**
     * Thanh toán qua PayOS
     */
    public PayOSPaymentInfoDto createPayOSPayment(Long invoiceId, PaymentRequestDto requestDto) {
        // 1. Validate invoice
        Invoice invoice = getInvoiceAndValidate(invoiceId, requestDto.getAmount());

        // 2. Tính số tiền còn phải trả
        Double totalPaid = getTotalPaid(invoice);
        Double amountRemaining = invoice.getTotalAmount() - totalPaid;

        if (requestDto.getAmount() > amountRemaining) {
            throw new CustomException(
                    String.format("Payment amount %.2f exceeds remaining amount %.2f",
                            requestDto.getAmount(), amountRemaining),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 3. Tạo payment transaction (PENDING)
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .amount(BigDecimal.valueOf(requestDto.getAmount()))
                .status(PaymentTransaction.Status.PENDING)
                .transactionType(PaymentTransaction.TransactionType.INVOICE_PAYOS)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        log.info("Created PayOS payment transaction {} for invoice {}",
                savedTransaction.getTransactionId(), invoiceId);

        // 4. Lấy thông tin renter
        Renter renter = invoice.getBooking().getRenter();

        // 5. Tạo payment với PayOS
        String description = String.format("HD %d EV", invoiceId);

        PayOSPaymentInfoDto payosPayment = payosPaymentService.createPayment(
                savedTransaction.getTransactionId(),
                requestDto.getAmount().intValue(),
                description,
                renter.getFullName(),
                renter.getEmail()
        );

        savedTransaction.setOrderCode(payosPayment.getOrderCode());
        paymentTransactionRepository.save(savedTransaction);

        // 6. Kiểm tra kết quả
        if (!"PENDING".equals(payosPayment.getStatus())) {
            // Tạo payment thất bại
            savedTransaction.setStatus(PaymentTransaction.Status.FAILED);
            paymentTransactionRepository.save(savedTransaction);

            log.error("PayOS payment creation failed for transaction {}",
                    savedTransaction.getTransactionId());
        }

        return payosPayment;
    }

    /**
     * Xử lý webhook từ PayOS
     */
    @Transactional
    public PayOSWebhookResponse handlePayOSWebhook(PayOSWebhookRequest webhookRequest) {

        log.info("Processing PayOS webhook - OrderCode: {}, Code: {}",
                webhookRequest.getData().getOrderCode(), webhookRequest.getData().getCode());

        try {
            // 1. Verify signature
            boolean isValidSignature = payosPaymentService.verifyWebhookSignature(webhookRequest);

            if (!isValidSignature) {
                log.error("Invalid PayOS webhook signature - OrderCode: {}",
                        webhookRequest.getData().getOrderCode());
                return payosPaymentService.createWebhookResponse(false, "Invalid signature");
            }

            // 2. Parse transaction ID từ order code
            // Trong production, query từ DB bằng orderCode
            Long orderCode = webhookRequest.getData().getOrderCode();
            PaymentTransaction transaction = paymentTransactionRepository
                    .findByOrderCode(orderCode)
                    .stream()
                    .filter(t -> t.getTransactionType() == PaymentTransaction.TransactionType.INVOICE_PAYOS)
                    .filter(t -> t.getStatus() == PaymentTransaction.Status.PENDING)
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Transaction not found for orderCode: " + orderCode));

            // 3. Kiểm tra transaction chưa xử lý (tránh duplicate webhook)
            if (transaction.getStatus() != PaymentTransaction.Status.PENDING) {
                log.warn("Transaction {} already processed, status: {}",
                        transaction.getTransactionId(), transaction.getStatus());
                return payosPaymentService.createWebhookResponse(true, "Already processed");
            }

            // 4. Xử lý theo result code
            if ("00".equals(webhookRequest.getData().getCode())) {
                // Thanh toán thành công
                handleSuccessfulPayOSPayment(transaction, webhookRequest);
            } else {
                // Thanh toán thất bại
                handleFailedPayOSPayment(transaction, webhookRequest);
            }

            // 5. Trả về response cho PayOS
            return payosPaymentService.createWebhookResponse(true, "Processed successfully");

        } catch (Exception e) {
            log.error("Error processing PayOS webhook - OrderCode: {}",
                    webhookRequest.getData().getOrderCode(), e);
            return payosPaymentService.createWebhookResponse(false, "Error: " + e.getMessage());
        }
    }

    // Helper methods

    private void updateInvoiceStatus(Invoice invoice, Double paidAmount) {
        // Tính tổng số tiền đã thanh toán
        Double totalPaid = invoice.getTransactions().stream()
                .filter(t -> t.getStatus() == PaymentTransaction.Status.SUCCESS)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        Double amountRemaining = invoice.getTotalAmount() - totalPaid;

        if (amountRemaining <= 0) {
            invoice.setStatus(Invoice.Status.PAID);
            invoice.setCompletedAt(LocalDateTime.now());

            // Hoàn cọc nếu là final invoice
            if (invoice.getType() == Invoice.Type.FINAL && invoice.getDepositAmount() > 0) {
                refundDeposit(invoice);
            }
        } else if (totalPaid > 0) {
            invoice.setStatus(Invoice.Status.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
    }

    private PaymentResponseDto mapToPaymentResponseDto(PaymentTransaction transaction) {
        return PaymentResponseDto.builder()
                .transactionId(transaction.getTransactionId())
                .invoiceId(transaction.getInvoice().getInvoiceId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .transactionType(transaction.getTransactionType())
                .transactionTime(transaction.getTransactionTime())
                .message("Payment processed successfully")
                .build();
    }

    private TransactionResponseDto mapToTransactionResponseDto(PaymentTransaction transaction) {
        return TransactionResponseDto.builder()
                .transactionId(transaction.getTransactionId())
                .invoiceId(transaction.getInvoice() != null ?
                        transaction.getInvoice().getInvoiceId() : null)
                .walletId(transaction.getWallet() != null ?
                        transaction.getWallet().getWalletId() : null)
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .transactionType(transaction.getTransactionType())
                .transactionTime(transaction.getTransactionTime())
                .build();
    }

    /**
     * Xử lý thanh toán thành công
     */
    private void handleSuccessfulPayment(PaymentTransaction transaction, MomoIPNRequest ipnRequest) {

        log.info("Processing successful payment - TransactionId: {}, MoMoTransId: {}",
                transaction.getTransactionId(), ipnRequest.getTransId());

        // 1. Cập nhật transaction status
        transaction.setStatus(PaymentTransaction.Status.SUCCESS);
        paymentTransactionRepository.save(transaction);

        // 2. Cập nhật invoice status
        Invoice invoice = transaction.getInvoice();
        updateInvoiceStatus(invoice);

        // 3. Gửi thông báo
        notificationService.sendPaymentSuccess(
                invoice.getBooking(),
                transaction.getAmount().doubleValue()
        );

        // 4. Nếu là final invoice và đã thanh toán đủ → hoàn cọc
        if (invoice.getType() == Invoice.Type.FINAL
                && invoice.getStatus() == Invoice.Status.PAID) {
            refundDeposit(invoice);
        }

        log.info("Payment processed successfully - TransactionId: {}",
                transaction.getTransactionId());
    }

    private void handleSuccessfulPayOSPayment(PaymentTransaction transaction, PayOSWebhookRequest ipnRequest) {

        log.info("Processing successful payment - TransactionId: {}, MoMoTransId: {}",
                transaction.getTransactionId(), ipnRequest.getCode());

        // 1. Cập nhật transaction status
        transaction.setStatus(PaymentTransaction.Status.SUCCESS);
        paymentTransactionRepository.save(transaction);

        // 2. Cập nhật invoice status
        Invoice invoice = transaction.getInvoice();
        updateInvoiceStatus(invoice);

        // 3. Gửi thông báo
        notificationService.sendPaymentSuccess(
                invoice.getBooking(),
                transaction.getAmount().doubleValue()
        );

        // 4. Nếu là final invoice và đã thanh toán đủ → hoàn cọc
        if (invoice.getType() == Invoice.Type.FINAL
                && invoice.getStatus() == Invoice.Status.PAID) {
            refundDeposit(invoice);
        }

        log.info("Payment processed successfully - TransactionId: {}",
                transaction.getTransactionId());
    }

    /**
     * Xử lý thanh toán thất bại
     */
    private void handleFailedPayment(PaymentTransaction transaction, MomoIPNRequest ipnRequest) {

        log.warn("Processing failed payment - TransactionId: {}, Reason: {}",
                transaction.getTransactionId(), ipnRequest.getMessage());

        // Cập nhật transaction status
        transaction.setStatus(PaymentTransaction.Status.FAILED);
        paymentTransactionRepository.save(transaction);

        // Gửi thông báo thanh toán thất bại
        Invoice invoice = transaction.getInvoice();
        Notification notification = Notification.builder()
                .title("❌ Thanh toán thất bại")
                .message(String.format(
                        "Thanh toán %.0f VND cho hóa đơn #%d đã thất bại. Lý do: %s",
                        transaction.getAmount().doubleValue(),
                        invoice.getInvoiceId(),
                        ipnRequest.getMessage()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(invoice.getBooking().getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Cập nhật trạng thái invoice sau khi thanh toán
     */
    private void updateInvoiceStatus(Invoice invoice) {
        Double totalPaid = getTotalPaid(invoice);
        Double amountRemaining = invoice.getTotalAmount() - totalPaid;

        if (amountRemaining <= 0) {
            // Đã thanh toán đủ
            invoice.setStatus(Invoice.Status.PAID);
            invoice.setCompletedAt(LocalDateTime.now());

            // Cập nhật deposit status
            if (invoice.getType() == Invoice.Type.DEPOSIT) {
                Booking booking = invoice.getBooking();
                booking.setDepositStatus(Booking.DepositStatus.PAID);
                booking.setStatus(Booking.Status.RESERVED);
                bookingRepository.save(booking);
            }

        } else if (totalPaid > 0) {
            // Đã thanh toán một phần
            invoice.setStatus(Invoice.Status.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Invoice {} status updated to {}", invoice.getInvoiceId(), invoice.getStatus());
    }

    /**
     * Hoàn tiền cọc vào ví
     */
    private void refundDeposit(Invoice invoice) {
        if (invoice.getDepositAmount() == null || invoice.getDepositAmount() <= 0) {
            return;
        }

        Booking booking = invoice.getBooking();
        Renter renter = booking.getRenter();

        // Tìm wallet
        Wallet wallet = walletRepository.findByRenter(renter).orElse(null);

        if (wallet != null && wallet.getStatus() == Wallet.Status.ACTIVE) {
            // Hoàn cọc vào ví
            BigDecimal refundAmount = BigDecimal.valueOf(invoice.getDepositAmount());
            wallet.setBalance(wallet.getBalance().add(refundAmount));
            walletRepository.save(wallet);

            // Tạo transaction hoàn cọc
            PaymentTransaction refundTransaction = PaymentTransaction.builder()
                    .invoice(invoice)
                    .wallet(wallet)
                    .amount(refundAmount)
                    .status(PaymentTransaction.Status.SUCCESS)
                    .transactionType(PaymentTransaction.TransactionType.WALLET_TOPUP)
                    .build();

            paymentTransactionRepository.save(refundTransaction);

            // Cập nhật booking
            booking.setDepositStatus(Booking.DepositStatus.REFUNDED);
            bookingRepository.save(booking);

            // Gửi thông báo
            notificationService.sendDepositRefunded(booking, invoice.getDepositAmount());

            log.info("Deposit refunded to wallet - Amount: {}, WalletId: {}",
                    refundAmount, wallet.getWalletId());
        }
    }

    /**
     * Tính tổng số tiền đã thanh toán cho invoice
     */
    private Double getTotalPaid(Invoice invoice) {
        return invoice.getTransactions().stream()
                .filter(t -> t.getStatus() == PaymentTransaction.Status.SUCCESS)
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();
    }

    /**
     * Validate invoice trước khi thanh toán
     */
    private Invoice getInvoiceAndValidate(Long invoiceId, Double amount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Invoice is already fully paid", HttpStatus.BAD_REQUEST);
        }

        if (amount <= 0) {
            throw new CustomException("Payment amount must be greater than 0",
                    HttpStatus.BAD_REQUEST);
        }

        return invoice;
    }

    /**
     * Xử lý thanh toán PayOS thất bại
     */
    private void handleFailedPayOSPayment(PaymentTransaction transaction, PayOSWebhookRequest webhookRequest) {

        log.warn("Processing failed PayOS payment - TransactionId: {}, Reason: {}",
                transaction.getTransactionId(), webhookRequest.getData().getDesc());

        // Cập nhật transaction status
        transaction.setStatus(PaymentTransaction.Status.FAILED);
        paymentTransactionRepository.save(transaction);

        // Gửi thông báo thanh toán thất bại
        Invoice invoice = transaction.getInvoice();
        Notification notification = Notification.builder()
                .title("❌ Thanh toán PayOS thất bại")
                .message(String.format(
                        "Thanh toán %d VND cho hóa đơn #%d đã thất bại. Lý do: %s",
                        transaction.getAmount().intValue(),
                        invoice.getInvoiceId(),
                        webhookRequest.getData().getDesc()
                ))
                .recipientType(Notification.RecipientType.RENTER)
                .recipientId(invoice.getBooking().getRenter().getRenterId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // Inject thêm các repository cần thiết
    private final NotificationRepository notificationRepository;

}
