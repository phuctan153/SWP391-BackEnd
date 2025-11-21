package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.entity.Policy.PolicyType;
import com.example.ev_rental_backend.entity.Policy.Status;
import com.example.ev_rental_backend.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    // ===========================================================
    // 🔹 Lấy giá trị của policy theo loại
    // ===========================================================
    @Override
    public double getPolicyValue(PolicyType type) {
        Policy active = getActivePolicyByType(type);
        return active.getValue();
    }

    // ===========================================================
    // 🔹 CRUD cơ bản
    // ===========================================================
    @Override
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    @Override
    public Policy getPolicyById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy policy #" + id));
    }

    @Override
    public Policy createPolicy(Policy policy) {
        // ✅ Gán thời gian hiện tại nếu chưa có
        LocalDateTime now = LocalDateTime.now();
        policy.setCreatedAt(now);
        policy.setUpdatedAt(now);

        // ✅ Nếu policy mới là ACTIVE → deactivate các policy cùng loại khác
        if (policy.getStatus() == Status.ACTIVE) {
            List<Policy> sameTypePolicies = policyRepository.findByPolicyType(policy.getPolicyType());
            sameTypePolicies.forEach(p -> p.setStatus(Status.INACTIVE));
            policyRepository.saveAll(sameTypePolicies);
        }

        // ✅ Lưu policy mới
        return policyRepository.save(policy);
    }


    @Override
    public Policy updatePolicy(Long id, Policy updatedPolicy) {
        Policy existing = getPolicyById(id);

        existing.setDescription(updatedPolicy.getDescription());
        existing.setValue(updatedPolicy.getValue());
        existing.setAppliedScope(updatedPolicy.getAppliedScope());
        existing.setStatus(updatedPolicy.getStatus());

        return policyRepository.save(existing);
    }

    @Override
    public Policy deactivatePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policy.setStatus(Status.INACTIVE);
        return policyRepository.save(policy);
    }

    // ===========================================================
    // 🔹 Hàm tiện ích
    // ===========================================================
    @Override
    public List<Policy> getPoliciesByType(PolicyType type) {
        return policyRepository.findByPolicyType(type);
    }

    @Override
    public Policy getActivePolicyByType(PolicyType type) {
        return policyRepository.findFirstByPolicyTypeAndStatusOrderByCreatedAtDesc(type, Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Không có policy đang hoạt động cho loại " + type));
    }
}
