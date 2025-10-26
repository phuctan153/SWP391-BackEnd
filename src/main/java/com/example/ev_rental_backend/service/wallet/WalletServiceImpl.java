package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final PriceListRepository priceListRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRepository; // 🟢 thêm repository để tìm booking

    @Override
    @Transactional
    public Wallet refundDepositFromPriceList(Long bookingId) {
        // 1️⃣ Tìm booking theo ID
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));

        // 2️⃣ Kiểm tra trạng thái cọc
        if (booking.getDepositStatus() == Booking.DepositStatus.REFUNDED) {
            throw new RuntimeException("Booking này đã được hoàn cọc trước đó!");
        }
        if (booking.getDepositStatus() != Booking.DepositStatus.PAID) {
            throw new RuntimeException("Booking này chưa thanh toán cọc, không thể hoàn!");
        }

        // 3️⃣ Lấy cấu hình tiền cọc trong PriceList
        PriceList depositPrice = priceListRepository.findByPriceType(PriceList.PriceType.DEPOSIT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình tiền cọc trong PriceList"));

        BigDecimal depositAmount = BigDecimal.valueOf(depositPrice.getUnitPrice());
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị tiền cọc không hợp lệ!");
        }

        // 4️⃣ Lấy ví của renter
        Renter renter = booking.getRenter();
        Wallet wallet = walletRepository.findByRenter_RenterId(renter.getRenterId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví cho renterId: " + renter.getRenterId()));

        if (wallet.getStatus() == Wallet.Status.INACTIVE) {
            throw new RuntimeException("Ví chưa được kích hoạt");
        }

        // 5️⃣ Cộng tiền hoàn cọc vào ví
        wallet.setBalance(wallet.getBalance().add(depositAmount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // 6️⃣ Ghi log PaymentTransaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .wallet(wallet)
                .amount(depositAmount)
                .transactionType(PaymentTransaction.TransactionType.WALLET_REFUND_DEPOSIT)
                .status(PaymentTransaction.Status.SUCCESS)
                .transactionTime(LocalDateTime.now())
                .build();

        paymentTransactionRepository.save(transaction);

        // 7️⃣ Cập nhật trạng thái cọc của booking → REFUNDED
        booking.setDepositStatus(Booking.DepositStatus.REFUNDED);
        bookingRepository.save(booking);

        return wallet;
    }
}
