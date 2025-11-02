package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.*;
import com.example.ev_rental_backend.repository.*;
import com.example.ev_rental_backend.service.policy.PolicyService;
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
    private final BookingRepository bookingRepository;
    private final PolicyRepository policyRepository;
    private final PolicyService policyService;

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
    public Wallet refundDepositWhenAdminCancels(Long bookingId) {
        // 🔹 Lấy booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking #" + bookingId));

        Renter renter = booking.getRenter();
        Wallet wallet = walletRepository.findByRenter(renter)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví của renter #" + renter.getRenterId()));

        if (wallet.getStatus() == Wallet.Status.INACTIVE) {
            throw new RuntimeException("Ví đang bị vô hiệu hóa, không thể hoàn tiền.");
        }

        // 🔹 Lấy chính sách doanh nghiệp
        Policy policy = policyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách doanh nghiệp"));

        double refundPercent = policyService.getPolicyValue(Policy.PolicyType.REFUND_PERCENT_ADMIN);
        double depositAmount = policyService.getPolicyValue(Policy.PolicyType.DEPOSIT_AMOUNT);


        BigDecimal refundAmount = BigDecimal.valueOf(depositAmount * (refundPercent / 100));

        // 🔹 Cập nhật ví
        wallet.setBalance(wallet.getBalance().add(refundAmount));

        // 🔹 Ghi nhận giao dịch hoàn tiền
        PaymentTransaction transaction = PaymentTransaction.builder()
                .wallet(wallet)
                .amount(refundAmount)
                .transactionTime(LocalDateTime.now())
                .transactionType(PaymentTransaction.TransactionType.WALLET_REFUND_DEPOSIT)
                .status(PaymentTransaction.Status.SUCCESS)
                .build();

        transactionRepository.save(transaction);
        walletRepository.save(wallet);

        return wallet;
    }

    @Override
    @Transactional
    public Wallet refundDepositWhenRenterCancels(Long bookingId) {
        // 🔹 Lấy booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking #" + bookingId));

        Renter renter = booking.getRenter();
        Wallet wallet = walletRepository.findByRenter(renter)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví của renter #" + renter.getRenterId()));

        if (wallet.getStatus() == Wallet.Status.INACTIVE) {
            throw new RuntimeException("Ví đang bị vô hiệu hóa, không thể hoàn tiền.");
        }

        // 🔹 Lấy policy hiện tại
        Policy policy = policyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách doanh nghiệp"));

        double refundPercent = policyService.getPolicyValue(Policy.PolicyType.REFUND_PERCENT_RENTER);

// ✅ Lấy tiền cọc hiện tại
        double depositAmount = policyService.getPolicyValue(Policy.PolicyType.DEPOSIT_AMOUNT);

        BigDecimal refundAmount = BigDecimal.valueOf(depositAmount * (refundPercent / 100));

        // 🔹 Cập nhật ví
        wallet.setBalance(wallet.getBalance().add(refundAmount));

        // 🔹 Ghi nhận giao dịch hoàn tiền
        PaymentTransaction transaction = PaymentTransaction.builder()
                .wallet(wallet)
                .amount(refundAmount)
                .transactionTime(LocalDateTime.now())
                .transactionType(PaymentTransaction.TransactionType.WALLET_REFUND_DEPOSIT)
                .status(PaymentTransaction.Status.SUCCESS)
                .build();

        transactionRepository.save(transaction);
        walletRepository.save(wallet);

        return wallet;
    }



}
