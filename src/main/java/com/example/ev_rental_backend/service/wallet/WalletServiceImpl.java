package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final RenterRepository renterRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Override
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    @Override
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví #" + id));
    }

    @Override
    @Transactional
    public Wallet createWallet(Long renterId) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy renter #" + renterId));

        if (walletRepository.findByRenter(renter).isPresent()) {
            throw new RuntimeException("Renter này đã có ví!");
        }

        Wallet wallet = Wallet.builder()
                .renter(renter)
                .balance(BigDecimal.ZERO)
                .status(Wallet.Status.INACTIVE)
                .build();

        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet updateBalance(Long id, BigDecimal amount, String action) {
        Wallet wallet = getWalletById(id);

        if (wallet.getStatus() == Wallet.Status.INACTIVE) {
            throw new RuntimeException("Ví đang bị vô hiệu hóa, không thể thao tác.");
        }

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setWallet(wallet);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setAmount(amount);

        switch (action.toLowerCase()) {
            case "deposit":
                wallet.setBalance(wallet.getBalance().add(amount));
                transaction.setTransactionType(PaymentTransaction.TransactionType.WALLET_TOPUP);
                transaction.setStatus(PaymentTransaction.Status.SUCCESS);
                break;

            case "withdraw":
                if (wallet.getBalance().compareTo(amount) < 0) {
                    transaction.setStatus(PaymentTransaction.Status.FAILED);
                    transaction.setTransactionType(PaymentTransaction.TransactionType.WALLET_WITHDRAW);
                    transactionRepository.save(transaction);
                    throw new RuntimeException("Số dư không đủ để rút.");
                }
                wallet.setBalance(wallet.getBalance().subtract(amount));
                transaction.setTransactionType(PaymentTransaction.TransactionType.WALLET_WITHDRAW);
                transaction.setStatus(PaymentTransaction.Status.SUCCESS);
                break;

            default:
                throw new RuntimeException("Hành động không hợp lệ (chỉ 'deposit' hoặc 'withdraw').");
        }

        transactionRepository.save(transaction);
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet activateWallet(Long id) {
        Wallet wallet = getWalletById(id);
        wallet.setStatus(Wallet.Status.ACTIVE);
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet deactivateWallet(Long id) {
        Wallet wallet = getWalletById(id);
        wallet.setStatus(Wallet.Status.INACTIVE);
        return walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public Wallet restoreWallet(Long id) {
        Wallet wallet = getWalletById(id);
        wallet.setStatus(Wallet.Status.ACTIVE);
        return walletRepository.save(wallet);
    }

    @Override
    public List<PaymentTransaction> getTransactionsByWalletId(Long walletId) {
        return transactionRepository.findByWallet_WalletId(walletId);
    }

    @Override
    @Transactional
    public Wallet refundDepositFromPriceList(Long bookingId) {
        // Ví dụ giả lập refund
        Wallet wallet = walletRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví để hoàn tiền."));

        BigDecimal refundAmount = new BigDecimal("50000");
        wallet.setBalance(wallet.getBalance().add(refundAmount));

        PaymentTransaction transaction = PaymentTransaction.builder()
                .wallet(wallet)
                .amount(refundAmount)
                .transactionTime(LocalDateTime.now())
                .status(PaymentTransaction.Status.SUCCESS)
                .transactionType(PaymentTransaction.TransactionType.WALLET_REFUND_DEPOSIT)
                .build();

        transactionRepository.save(transaction);
        return walletRepository.save(wallet);
    }
}
