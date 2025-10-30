package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    // ===========================================================
    // 🔹 Các hàm tiện ích cho Booking / Refund
    // ===========================================================

    @Override
    public double getRefundPercentForRenter() {
        Policy active = getActivePolicy();
        return active.getRefundPercentRenter();
    }

    @Override
    public double getRefundPercentForAdmin() {
        Policy active = getActivePolicy();
        return active.getRefundPercentAdmin();
    }

    @Override
    public Policy getActivePolicy() {
        return policyRepository.findFirstByStatusOrderByCreatedAtDesc(Policy.Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Không có policy nào đang hoạt động"));
    }

    @Override
    public double getDepositAmountForBooking(Long bookingId) {
        // BookingId chỉ để tham chiếu, tạm thời mọi booking dùng policy ACTIVE hiện tại
        Policy active = getActivePolicy();
        return active.getDepositAmount();
    }

    // ===========================================================
    // 🧩 CRUD
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
        // Nếu policy mới là ACTIVE → deactivate tất cả policy khác
        if (policy.getStatus() == Policy.Status.ACTIVE) {
            List<Policy> activePolicies = policyRepository.findByStatus(Policy.Status.ACTIVE);
            activePolicies.forEach(p -> p.setStatus(Policy.Status.INACTIVE));
            policyRepository.saveAll(activePolicies);
        }

        return policyRepository.save(policy);
    }

    @Override
    public Policy updatePolicy(Long id, Policy updatedPolicy) {
        Policy existing = getPolicyById(id);

        existing.setPolicyName(updatedPolicy.getPolicyName());
        existing.setDescription(updatedPolicy.getDescription());
        existing.setRefundPercentRenter(updatedPolicy.getRefundPercentRenter());
        existing.setRefundPercentAdmin(updatedPolicy.getRefundPercentAdmin());
        existing.setMinDaysBeforeBooking(updatedPolicy.getMinDaysBeforeBooking());
        existing.setMaxDaysBeforeBooking(updatedPolicy.getMaxDaysBeforeBooking());
        existing.setDepositAmount(updatedPolicy.getDepositAmount());
        existing.setAppliedScope(updatedPolicy.getAppliedScope());
        existing.setStatus(updatedPolicy.getStatus());

        return policyRepository.save(existing);
    }

    @Override
    public Policy deactivatePolicy(Long id) {
        Policy policy = getPolicyById(id);
        policy.setStatus(Policy.Status.INACTIVE);
        return policyRepository.save(policy);
    }
}
