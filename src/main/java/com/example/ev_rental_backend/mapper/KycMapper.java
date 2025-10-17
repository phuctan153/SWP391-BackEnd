package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.renter.KycVerificationDTO;
import com.example.ev_rental_backend.entity.IdentityDocument;
import com.example.ev_rental_backend.entity.Renter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface KycMapper {

    // Cập nhật thông tin cá nhân trong Renter
    @Mapping(target = "address", source = "driverAddress")
    @Mapping(target = "dateOfBirth", source = "nationalDob")
    void updateRenterFromKyc(KycVerificationDTO dto, @MappingTarget Renter renter);

    // Tạo bản ghi cho CCCD (National ID)
    @Mapping(target = "type", constant = "NATIONAL_ID")
    @Mapping(target = "documentNumber", source = "nationalId")
    @Mapping(target = "expiryDate", source = "nationalExpireDate")
    @Mapping(target = "status", constant = "PENDING")
    IdentityDocument toNationalIdDocument(KycVerificationDTO dto);

    // Tạo bản ghi cho GPLX (Driver License)
    @Mapping(target = "type", constant = "DRIVER_LICENSE")
    @Mapping(target = "documentNumber", source = "driverLicense")
    @Mapping(target = "expiryDate", source = "driverExpireDate")
    @Mapping(target = "status", constant = "PENDING")
    IdentityDocument toDriverLicenseDocument(KycVerificationDTO dto);
}
