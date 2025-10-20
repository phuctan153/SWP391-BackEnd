package com.example.ev_rental_backend.service.contract;

import com.example.ev_rental_backend.dto.booking.BookingContractInfoDTO;
import com.example.ev_rental_backend.dto.contract.ContractRequestDTO;
import com.example.ev_rental_backend.dto.contract.ContractResponseDTO;
import com.example.ev_rental_backend.entity.Contract;

public interface ContractService {

    public ContractResponseDTO createContract(ContractRequestDTO dto);

    BookingContractInfoDTO getBookingInfoForContract(Long bookingId);

    void sendContractToAdmin(Long contractId);
}
