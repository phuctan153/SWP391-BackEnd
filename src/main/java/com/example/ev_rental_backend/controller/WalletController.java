package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // ✅ Hoàn cọc dựa trên PriceList
    @PostMapping("/refund-deposit/{renterId}")
    public ResponseEntity<ApiResponse<Wallet>> refundDeposit(@PathVariable Long renterId) {
        try {
            Wallet wallet = walletService.refundDepositFromPriceList(renterId);
            return ResponseEntity.ok(
                    ApiResponse.<Wallet>builder()
                            .status("success")
                            .code(200)
                            .data(wallet)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Wallet>builder()
                            .status("error")
                            .code(400)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.<Wallet>builder()
                            .status("error")
                            .code(500)
                            .message("Lỗi hệ thống: " + e.getMessage())
                            .build()
            );
        }
    }
}
