package com.example.ev_rental_backend.service.payment;

import com.example.ev_rental_backend.dto.payment.*;
import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.exception.CustomException;
import com.example.ev_rental_backend.exception.NotFoundException;
import com.example.ev_rental_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final WalletRepository walletRepository;
    private final RenterRepository renterRepository;
    private final MomoService momoPaymentService;
    private final BookingRepository bookingRepository;

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
     * Thanh toán qua MoMo (BR-30)
     */
    public MomoPaymentResponseDto payByMomo(Long invoiceId, PaymentRequestDto requestDto) {
        Invoice invoice = getInvoiceAndValidate(invoiceId, requestDto.getAmount());

        // Tạo transaction với trạng thái PENDING
        PaymentTransaction transaction = PaymentTransaction.builder()
                .invoice(invoice)
                .amount(BigDecimal.valueOf(requestDto.getAmount()))
                .status(PaymentTransaction.Status.PENDING)
                .transactionType(PaymentTransaction.TransactionType.INVOICE_MOMO)
                .build();

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        // Gọi MoMo API để tạo payment
        MomoPaymentResponseDto momoResponse = momoPaymentService.createPayment(
                savedTransaction.getTransactionId(),
                requestDto.getAmount(),
                "Payment for invoice #" + invoiceId
        );

        return momoResponse;
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
     * Xử lý callback từ MoMo
     */
    public void handleMomoCallback(MomoCallbackDto callbackDto) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findById(callbackDto.getTransactionId())
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (callbackDto.getResultCode() == 0) {
            // Thanh toán thành công
            transaction.setStatus(PaymentTransaction.Status.SUCCESS);
            paymentTransactionRepository.save(transaction);

            // Cập nhật invoice
            Invoice invoice = transaction.getInvoice();
            updateInvoiceStatus(invoice, transaction.getAmount().doubleValue());
        } else {
            // Thanh toán thất bại
            transaction.setStatus(PaymentTransaction.Status.FAILED);
            paymentTransactionRepository.save(transaction);
        }
    }

    // Helper methods

    private Invoice getInvoiceAndValidate(Long invoiceId, Double amount) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice not found with id: " + invoiceId));

        // Kiểm tra invoice chưa thanh toán đầy đủ
        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new CustomException("Invoice is already fully paid", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra số tiền hợp lệ
        if (amount <= 0) {
            throw new CustomException("Payment amount must be greater than 0",
                    HttpStatus.BAD_REQUEST);
        }

        return invoice;
    }

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

    private void refundDeposit(Invoice invoice) {
        Booking booking = invoice.getBooking();
        Renter renter = booking.getRenter();

        // Tìm wallet của renter
        Wallet wallet = walletRepository.findByRenter(renter).orElse(null);

        if (wallet != null && wallet.getStatus() == Wallet.Status.ACTIVE) {
            // Hoàn cọc vào ví
            wallet.setBalance(wallet.getBalance().add(BigDecimal.valueOf(invoice.getDepositAmount())));
            walletRepository.save(wallet);

            // Tạo transaction hoàn cọc
            PaymentTransaction refundTransaction = PaymentTransaction.builder()
                    .invoice(invoice)
                    .wallet(wallet)
                    .amount(BigDecimal.valueOf(invoice.getDepositAmount()))
                    .status(PaymentTransaction.Status.SUCCESS)
                    .transactionType(PaymentTransaction.TransactionType.WALLET_TOPUP)
                    .build();

            paymentTransactionRepository.save(refundTransaction);
        }

        // Cập nhật trạng thái deposit của booking
        booking.setDepositStatus(Booking.DepositStatus.REFUNDED);
        bookingRepository.save(booking);
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
}
