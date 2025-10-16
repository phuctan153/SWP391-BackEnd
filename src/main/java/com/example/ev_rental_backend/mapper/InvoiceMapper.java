package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.invoice.InvoiceResponseDTO;
import com.example.ev_rental_backend.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    /**
     * Map Invoice sang InvoiceResponseDTO
     */
    @Mapping(target = "type", expression = "java(invoice.getType().name())")
    @Mapping(target = "status", expression = "java(invoice.getStatus().name())")
    @Mapping(target = "lineItems", expression = "java(mapLineItems(invoice.getLines()))")
    @Mapping(target = "booking", source = "booking")
    @Mapping(target = "transactions", expression = "java(mapTransactions(invoice.getTransactions()))")
    InvoiceResponseDTO toDto(Invoice invoice);

    /**
     * Map Booking sang BookingBasicDTO
     */
    @Mapping(target = "bookingId", source = "bookingId")
    @Mapping(target = "vehicleName", source = "vehicle.vehicleName")
    @Mapping(target = "plateNumber", source = "vehicle.plateNumber")
    @Mapping(target = "renterName", source = "renter.fullName")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    InvoiceResponseDTO.BookingBasicDTO toBookingBasicDto(Booking booking);

    /**
     * Map List<InvoiceDetail> sang List<InvoiceLineDetailDTO>
     */
    default List<InvoiceResponseDTO.InvoiceLineDetailDTO> mapLineItems(List<InvoiceDetail> details) {
        if (details == null) {
            return null;
        }
        return details.stream()
                .map(this::toLineDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Map InvoiceDetail sang InvoiceLineDetailDTO
     */
    @Mapping(target = "type", expression = "java(detail.getType().name())")
    @Mapping(target = "sparePart", source = "sparePart")
    InvoiceResponseDTO.InvoiceLineDetailDTO toLineDetailDto(InvoiceDetail detail);

    /**
     * Map SparePart sang SparePartBasicDTO
     */
    @Mapping(target = "sparepartId", source = "sparepartId")
    @Mapping(target = "partName", source = "partName")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "unitPrice", source = "unitPrice")
    InvoiceResponseDTO.SparePartBasicDTO toSparePartBasicDto(SparePart sparePart);

    /**
     * Map List<PaymentTransaction> sang List<PaymentTransactionDTO>
     */
    default List<InvoiceResponseDTO.PaymentTransactionDTO> mapTransactions(List<PaymentTransaction> transactions) {
        if (transactions == null) {
            return null;
        }
        return transactions.stream()
                .map(this::toTransactionDto)
                .collect(Collectors.toList());
    }

    /**
     * Map PaymentTransaction sang PaymentTransactionDTO
     */
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    InvoiceResponseDTO.PaymentTransactionDTO toTransactionDto(PaymentTransaction transaction);
}
