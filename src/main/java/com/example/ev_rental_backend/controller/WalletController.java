package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.payment.PaymentTransactionResponseDto;
import com.example.ev_rental_backend.dto.wallet.WalletResponseDTO;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.Wallet;
import com.example.ev_rental_backend.mapper.WalletMapper;
import com.example.ev_rental_backend.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    // 🟢 ADMIN xem tất cả ví
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponseDTO>>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        List<WalletResponseDTO> dtos = wallets.stream().map(walletMapper::toDto).toList();

        return ResponseEntity.ok(ApiResponse.<List<WalletResponseDTO>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(dtos)
                .message("Lấy danh sách ví thành công (ADMIN)")
                .build());
    }

    // 🟢 Xem chi tiết ví
    @PreAuthorize("hasAnyRole('ADMIN','RENTER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> getWalletById(
            @PathVariable Long id,
            Authentication authentication) {

        Wallet wallet = walletService.getWalletById(id);

        // Nếu là renter thì chỉ được xem ví của chính mình
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RENTER"))) {
            String email = authentication.getName(); // email của renter hiện tại
            if (!wallet.getRenter().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.<WalletResponseDTO>builder()
                                .status("error")
                                .code(HttpStatus.FORBIDDEN.value())
                                .message("Bạn không có quyền xem ví của người khác!")
                                .build()
                );
            }
        }

        WalletResponseDTO dto = walletMapper.toDto(wallet);
        return ResponseEntity.ok(ApiResponse.<WalletResponseDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(dto)
                .message("Lấy thông tin ví thành công")
                .build());
    }

    // 🟡 Tạo ví (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create/{renterId}")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> createWallet(@PathVariable Long renterId) {
        Wallet wallet = walletService.createWallet(renterId);
        WalletResponseDTO dto = walletMapper.toDto(wallet);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<WalletResponseDTO>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(dto)
                .message("Tạo ví mới thành công cho renter #" + renterId)
                .build());
    }

    // 🟠 Nạp / Rút tiền — renter chỉ được thao tác ví của chính mình
    @PreAuthorize("hasAnyRole('ADMIN','RENTER')")
    @PutMapping("/{id}/update-balance")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> updateBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String action,
            Authentication authentication
    ) {
        Wallet wallet = walletService.getWalletById(id);

        // Renter chỉ được cập nhật ví của chính mình
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RENTER"))) {
            String email = authentication.getName();
            if (!wallet.getRenter().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.<WalletResponseDTO>builder()
                                .status("error")
                                .code(HttpStatus.FORBIDDEN.value())
                                .message("Bạn không thể nạp/rút ví của người khác!")
                                .build()
                );
            }
        }

        Wallet updated = walletService.updateBalance(id, amount, action);
        WalletResponseDTO dto = walletMapper.toDto(updated);

        return ResponseEntity.ok(ApiResponse.<WalletResponseDTO>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(dto)
                .message("Cập nhật số dư ví thành công")
                .build());
    }

    // 🔵 ADMIN có thể kích hoạt / vô hiệu / khôi phục ví
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> activateWallet(@PathVariable Long id) {
        Wallet wallet = walletService.activateWallet(id);
        WalletResponseDTO dto = walletMapper.toDto(wallet);
        return ResponseEntity.ok(ApiResponse.<WalletResponseDTO>builder()
                .status("success").code(HttpStatus.OK.value()).data(dto).message("Kích hoạt ví thành công").build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> deactivateWallet(@PathVariable Long id) {
        Wallet wallet = walletService.deactivateWallet(id);
        WalletResponseDTO dto = walletMapper.toDto(wallet);
        return ResponseEntity.ok(ApiResponse.<WalletResponseDTO>builder()
                .status("success").code(HttpStatus.OK.value()).data(dto).message("Đã vô hiệu hóa ví #" + id).build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<WalletResponseDTO>> restoreWallet(@PathVariable Long id) {
        Wallet wallet = walletService.restoreWallet(id);
        WalletResponseDTO dto = walletMapper.toDto(wallet);
        return ResponseEntity.ok(ApiResponse.<WalletResponseDTO>builder()
                .status("success").code(HttpStatus.OK.value()).data(dto).message("Đã khôi phục ví #" + id).build());
    }

    // 🧾 Lịch sử giao dịch — renter chỉ xem ví của chính mình
    @PreAuthorize("hasAnyRole('ADMIN','RENTER')")
    @GetMapping("/{id}/transactions")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponseDto>>> getTransactions(
            @PathVariable Long id,
            Authentication authentication) {

        Wallet wallet = walletService.getWalletById(id);

        // Renter không được xem giao dịch của ví khác
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RENTER"))) {
            String email = authentication.getName();
            if (!wallet.getRenter().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        ApiResponse.<List<PaymentTransactionResponseDto>>builder()
                                .status("error")
                                .code(HttpStatus.FORBIDDEN.value())
                                .message("Bạn không thể xem giao dịch của ví người khác!")
                                .build()
                );
            }
        }

        List<PaymentTransactionResponseDto> transactions =
                walletService.getTransactionsByWalletId(id);

        return ResponseEntity.ok(ApiResponse.<List<PaymentTransactionResponseDto>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(transactions)
                .message("Lấy lịch sử giao dịch thành công")
                .build());
    }

    // 💰 Hoàn tiền (ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund/admin-cancel/{bookingId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refundDepositByBooking(@PathVariable Long bookingId) {
        Wallet wallet = walletService.refundDepositWhenAdminCancels(bookingId);
        WalletResponseDTO dto = walletMapper.toDto(wallet);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", bookingId);
        data.put("wallet", dto);
        data.put("message", "Hoàn tiền đặt cọc thành công cho booking #" + bookingId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder().status("success").code(HttpStatus.OK.value()).data(data).build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/refund/renter-cancel/{bookingId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refundDepositByRenter(@PathVariable Long bookingId) {
        Wallet wallet = walletService.refundDepositWhenRenterCancels(bookingId);
        WalletResponseDTO dto = walletMapper.toDto(wallet);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", bookingId);
        data.put("wallet", dto);
        data.put("message", "Hoàn tiền đặt cọc thành công cho renter từ booking #" + bookingId);
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder().status("success").code(HttpStatus.OK.value()).data(data).build());
    }
}
