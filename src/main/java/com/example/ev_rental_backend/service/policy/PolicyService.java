package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.entity.Policy.PolicyType;

import java.util.List;

public interface PolicyService {

    // 🔹 Lấy giá trị của 1 policy đang hoạt động theo loại
    double getPolicyValue(PolicyType type);

    // 🔹 Lấy tất cả policy
    List<Policy> getAllPolicies();

    // 🔹 Lấy 1 policy theo ID
    Policy getPolicyById(Long id);

    // 🔹 Tạo mới policy
    Policy createPolicy(Policy policy);

    // 🔹 Cập nhật policy
    Policy updatePolicy(Long id, Policy updatedPolicy);

    // 🔹 Đổi trạng thái INACTIVE
    Policy deactivatePolicy(Long id);

    // 🔹 Lấy danh sách policy theo loại
    List<Policy> getPoliciesByType(PolicyType type);

    // 🔹 Lấy policy đang active theo loại
    Policy getActivePolicyByType(PolicyType type);
}
