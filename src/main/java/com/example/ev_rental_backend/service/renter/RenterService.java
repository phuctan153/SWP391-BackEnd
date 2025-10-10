package com.example.ev_rental_backend.service.renter;

import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.dto.renter.RenterRequestDTO;
import com.example.ev_rental_backend.dto.renter.RenterResponseDTO;
import com.example.ev_rental_backend.entity.Renter;

public interface RenterService {
    RenterResponseDTO registerRenter(RenterRequestDTO dto);
    RenterResponseDTO loginRenter(String email, String password); // 🟢 thêm dòng này

    public Renter verifyKyc(KycVerificationDTO dto);

    public String checkKycStatus(Long renterId);


}
