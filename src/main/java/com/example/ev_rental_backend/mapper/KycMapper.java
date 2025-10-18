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
}
