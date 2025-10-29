package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    // 🟢 Lấy danh sách ví
    @GetMapping
    public ResponseEntity<ApiResponse<List<Wallet>>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(ApiResponse.<List<Wallet>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallets)
                .build());
    }

    // 🟢 Xem chi tiết ví
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Wallet>> getWalletById(@PathVariable Long id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallet)
                .build());
    }

    // 🟡 Tạo ví mới
    @PostMapping("/create/{renterId}")
    public ResponseEntity<ApiResponse<Wallet>> createWallet(@PathVariable Long renterId) {
        Wallet wallet = walletService.createWallet(renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(wallet)
                .message("Tạo ví mới thành công cho renter #" + renterId)
                .build());
    }

    // 🟠 Nạp / Rút tiền
    @PutMapping("/{id}/update-balance")
    public ResponseEntity<ApiResponse<Wallet>> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String action
    ) {
        Wallet wallet = walletService.updateBalance(id, amount, action);
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallet)
                .message("Cập nhật số dư ví thành công")
                .build());
    }

    // 🔵 Kích hoạt ví
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Wallet>> activateWallet(@PathVariable Long id) {
        Wallet wallet = walletService.activateWallet(id);
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallet)
                .message("Kích hoạt ví thành công")
                .build());
    }

    // 🔴 Vô hiệu hóa ví
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Wallet>> deactivateWallet(@PathVariable Long id) {
        Wallet wallet = walletService.deactivateWallet(id);
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallet)
                .message("Đã vô hiệu hóa ví #" + id)
                .build());
    }

    // 🟢 Khôi phục ví
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Wallet>> restoreWallet(@PathVariable Long id) {
        Wallet wallet = walletService.restoreWallet(id);
        return ResponseEntity.ok(ApiResponse.<Wallet>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(wallet)
                .message("Đã khôi phục ví #" + id)
                .build());
    }

    // 🧾 Lịch sử giao dịch của ví
    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<PaymentTransaction>>> getTransactions(@PathVariable Long id) {
        List<PaymentTransaction> transactions = walletService.getTransactionsByWalletId(id);
        return ResponseEntity.ok(ApiResponse.<List<PaymentTransaction>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(transactions)
                .message("Lấy lịch sử giao dịch của ví #" + id + " thành công")
                .build());
    }
}