package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.entity.Policy.PolicyType;
import com.example.ev_rental_backend.service.policy.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://swp-391-frontend-mu.vercel.app", allowCredentials = "true")
public class PolicyController {

    private final PolicyService policyService;

    // ===========================================================
    // 🔹 Lấy tất cả policy
    // ===========================================================
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<Policy>>> getAllPolicies() {
        List<Policy> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.<List<Policy>>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(policies)
                .message("Lấy danh sách chính sách thành công")
                .build());
    }

    // ===========================================================
    // 🔹 Lấy policy theo ID
    // ===========================================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Policy>> getPolicyById(@PathVariable Long id) {
        Policy policy = policyService.getPolicyById(id);
        return ResponseEntity.ok(ApiResponse.<Policy>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(policy)
                .message("Lấy chi tiết chính sách thành công")
                .build());
    }

    // ===========================================================
    // 🔹 Tạo mới policy
    // ===========================================================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Policy>> createPolicy(@RequestBody Policy policy) {
        Policy created = policyService.createPolicy(policy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<Policy>builder()
                .status("success")
                .code(HttpStatus.CREATED.value())
                .data(created)
                .message("Tạo chính sách mới thành công")
                .build());
    }

    // ===========================================================
    // 🔹 Cập nhật policy
    // ===========================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Policy>> updatePolicy(
            @PathVariable Long id,
            @RequestBody Policy updatedPolicy) {

        Policy updated = policyService.updatePolicy(id, updatedPolicy);
        return ResponseEntity.ok(ApiResponse.<Policy>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(updated)
                .message("Cập nhật chính sách thành công")
                .build());
    }

    // ===========================================================
    // 🔹 Xóa mềm (INACTIVE)
    // ===========================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Policy>> deactivatePolicy(@PathVariable Long id) {
        Policy deactivated = policyService.deactivatePolicy(id);
        return ResponseEntity.ok(ApiResponse.<Policy>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(deactivated)
                .message("Chính sách #" + id + " đã được chuyển sang INACTIVE")
                .build());
    }

    // ===========================================================
    // 🔹 Lấy policy active theo loại
    // ===========================================================
    @GetMapping("/active/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Policy>> getActivePolicyByType(@PathVariable PolicyType type) {
        Policy active = policyService.getActivePolicyByType(type);
        return ResponseEntity.ok(ApiResponse.<Policy>builder()
                .status("success")
                .code(HttpStatus.OK.value())
                .data(active)
                .message("Chính sách " + type + " đang hoạt động hiện tại")
                .build());
    }
}
