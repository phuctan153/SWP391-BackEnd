package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;
import com.example.ev_rental_backend.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    @Override
    public double getRefundPercentForRenter() {
        Double percent = policyRepository.getRefundPercentForRenter();
        return percent != null ? percent : 0.0;
    }

    @Override
    public double getRefundPercentForAdmin() {
        Double percent = policyRepository.getRefundPercentForAdmin();
        return percent != null ? percent : 0.0;
    }


    @Override
    public Policy getActivePolicy() {
        return policyRepository.getActivePolicy();
    }

    @Override
    public double getDepositAmountForBooking(Long bookingId) {
        Policy policy = policyRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chính sách doanh nghiệp"));
        return policy.getDepositAmount();
    }
}
