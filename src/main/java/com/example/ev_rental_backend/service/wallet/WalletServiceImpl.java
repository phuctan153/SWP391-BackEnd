package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.PriceList;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.repository.PaymentTransactionRepository;
import com.example.ev_rental_backend.repository.PriceListRepository;
import com.example.ev_rental_backend.repository.WalletRepository;
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

    @Override
    @Transactional
    public Wallet refundDepositFromPriceList(Long renterId) {
        // 1️⃣ Tìm PriceList có type = DEPOSIT
        PriceList depositPrice = priceListRepository.findByPriceType(PriceList.PriceType.DEPOSIT)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cấu hình tiền cọc trong PriceList"));

        BigDecimal depositAmount = BigDecimal.valueOf(depositPrice.getUnitPrice());
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị cọc không hợp lệ");
        }

        // 2️⃣ Tìm ví của renter
        Wallet wallet = walletRepository.findByRenter_RenterId(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví cho renterId: " + renterId));

        if (wallet.getStatus() == Wallet.Status.INACTIVE) {
            throw new RuntimeException("Ví chưa được kích hoạt");
        }

        // 3️⃣ Cộng tiền hoàn cọc vào ví
        BigDecimal newBalance = wallet.getBalance().add(depositAmount);
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(LocalDateTime.now());

        // 4️⃣ Ghi log PaymentTransaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .wallet(wallet)
                .amount(depositAmount)
                .transactionType(PaymentTransaction.TransactionType.WALLET_REFUND_DEPOSIT)
                .status(PaymentTransaction.Status.SUCCESS)
                .transactionTime(LocalDateTime.now())
                .build();

        paymentTransactionRepository.save(transaction);
        walletRepository.save(wallet);

        return wallet;
    }
}
