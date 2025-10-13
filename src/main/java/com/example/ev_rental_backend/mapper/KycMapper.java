package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.entity.Renter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KycMapper {

    @Mapping(target = "nationalId", source = "nationalId")
    @Mapping(target = "driverLicense", source = "driverLicense")
    @Mapping(target = "address", source = "driverAddress")
    @Mapping(target = "dateOfBirth", source = "nationalDob")
    @Mapping(target = "driverLicenseExpiry", source = "driverExpireDate")
    void updateRenterFromKyc(KycVerificationDTO dto, @MappingTarget Renter renter);
}
