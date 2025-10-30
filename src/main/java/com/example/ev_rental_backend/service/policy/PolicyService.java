package com.example.ev_rental_backend.service.policy;

import com.example.ev_rental_backend.entity.Policy;

public interface PolicyService {
    public double getRefundPercentForRenter();
    public double getRefundPercentForAdmin();
    public Policy getActivePolicy();

    double getDepositAmountForBooking(Long bookingId);
}
