package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;

import java.util.List;

public interface PolicyService {

    // 🔹 Lấy phần trăm hoàn tiền khi renter hủy booking
    double getRefundPercentForRenter();

    // 🔹 Lấy phần trăm hoàn tiền khi admin hủy booking
    double getRefundPercentForAdmin();

    // 🔹 Lấy policy đang hoạt động (ACTIVE)
    Policy getActivePolicy();

    // 🔹 Lấy tiền cọc hiện tại (dùng khi renter đặt xe)
    double getDepositAmountForBooking(Long bookingId);

    // ================= CRUD =================

    // Lấy tất cả policy
    List<Policy> getAllPolicies();

    // Lấy 1 policy theo ID
    Policy getPolicyById(Long id);

    // Tạo mới (nếu ACTIVE thì tự động deactivate các policy cũ)
    Policy createPolicy(Policy policy);

    // Cập nhật thông tin
    Policy updatePolicy(Long id, Policy updatedPolicy);

    // Xóa mềm (set status = INACTIVE)
    Policy deactivatePolicy(Long id);
}
