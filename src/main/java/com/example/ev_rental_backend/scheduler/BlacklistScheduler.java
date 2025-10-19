package com.example.ev_rental_backend.scheduler;

import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.RenterRepository;
import com.example.ev_rental_backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlacklistScheduler {

    private final RenterRepository renterRepository;
    private final WalletRepository walletRepository;

    /**
     * 🔄 Chạy tự động mỗi ngày lúc 00:00
     */
    @Scheduled(cron = "0 0 0 * * *") // mỗi ngày lúc 0h
    public void unlockBlacklistedRenters() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        List<Renter> renters = renterRepository.findBlacklistedOver6Months(sixMonthsAgo);
        if (renters.isEmpty()) {
            log.info("Không có renter nào đủ điều kiện mở khóa hôm nay.");
            return;
        }

        for (Renter renter : renters) {
            renter.setBlacklisted(false);
            renter.setStatus(Renter.Status.VERIFIED);
            renterRepository.save(renter);

            if (renter.getWallet() != null) {
                renter.getWallet().setStatus(com.example.ev_rental_backend.entity.Wallet.Status.ACTIVE);
                walletRepository.save(renter.getWallet());
            }

            log.info("✅ Đã mở khóa renter ID={} - {}", renter.getRenterId(), renter.getEmail());
        }
    }
}
