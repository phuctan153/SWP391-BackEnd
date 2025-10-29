package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.Wallet;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    List<Wallet> getAllWallets();

    Wallet getWalletById(Long id);

    Wallet createWallet(Long renterId);

    Wallet updateBalance(Long id, BigDecimal amount, String action);

    Wallet activateWallet(Long id);

    Wallet deactivateWallet(Long id);

    Wallet restoreWallet(Long id);

    List<PaymentTransaction> getTransactionsByWalletId(Long walletId);
    Wallet refundDepositFromPriceList(Long bookingId);
}