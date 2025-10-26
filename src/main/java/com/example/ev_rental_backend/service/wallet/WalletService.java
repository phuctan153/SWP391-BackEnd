package com.example.ev_rental_backend.service.wallet;

import com.example.ev_rental_backend.entity.Wallet;

public interface WalletService {
    Wallet refundDepositFromPriceList(Long bookingId);
}