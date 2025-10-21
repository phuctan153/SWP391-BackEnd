package com.example.ev_rental_backend.service.contract;

import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.contract.AdminContractSignDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;

import java.util.List;

public interface ContractService {

    public ContractResponseDTO createContract(ContractRequestDTO dto);

    BookingContractInfoDTO getBookingInfoForContract(Long bookingId);

    void sendContractToAdmin(Long contractId);

    public List<BookingContractInfoDTO> getContractsByStatus(String status);

    public void sendOtpForAdminSignature(Long contractId, Long adminId);

    public void verifyAdminSignature(AdminContractSignDTO dto);
}
