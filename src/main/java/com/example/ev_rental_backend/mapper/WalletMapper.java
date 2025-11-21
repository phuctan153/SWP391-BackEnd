package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.wallet.WalletResponseDTO;
import com.example.ev_rental_backend.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(source = "renter.renterId", target = "renterId")
    @Mapping(source = "renter.fullName", target = "renterName")
    @Mapping(source = "renter.email", target = "renterEmail")
    WalletResponseDTO toDto(Wallet wallet);
}
