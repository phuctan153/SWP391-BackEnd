package com.example.ev_rental_backend.mapper;

import com.example.ev_rental_backend.dto.invoice.InvoiceSummaryDTO;
import com.example.ev_rental_backend.dto.payment.PaymentResponseDTO;
import com.example.ev_rental_backend.entity.Invoice;
import com.example.ev_rental_backend.entity.InvoiceDetail;
import com.example.ev_rental_backend.entity.PaymentTransaction;
import com.example.ev_rental_backend.entity.SparePart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Map PaymentTransaction sang PaymentResponseDTO
     */
    @Mapping(target = "transactionId", source = "transactionId")
    @Mapping(target = "invoiceId", source = "invoice.invoiceId")
    @Mapping(target = "bookingId", source = "invoice.booking.bookingId")
    @Mapping(target = "invoiceType", expression = "java(transaction.getInvoice().getType().name())")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "status", expression = "java(transaction.getStatus().name())")
    @Mapping(target = "createdAt", source = "transactionTime")
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "payUrl", ignore = true)
    @Mapping(target = "qrCodeUrl", ignore = true)
    @Mapping(target = "deeplink", ignore = true)
    @Mapping(target = "expiresAt", ignore = true)
    PaymentResponseDTO toPaymentResponseDTO(PaymentTransaction transaction);

    /**
     * Map Invoice sang InvoiceSummaryDTO
     */
    @Mapping(target = "invoiceType", expression = "java(invoice.getType().name())")
    @Mapping(target = "status", expression = "java(invoice.getStatus().name())")
    @Mapping(target = "lineItems", expression = "java(mapLineItems(invoice.getLines()))")
    InvoiceSummaryDTO toInvoiceSummaryDTO(Invoice invoice);

    /**
     * Map List<InvoiceDetail> sang List<LineItemDTO>
     */
    default List<InvoiceSummaryDTO.LineItemDTO> mapLineItems(List<InvoiceDetail> details) {
        if (details == null) {
            return null;
        }
        return details.stream()
                .map(this::toLineItemDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map InvoiceDetail sang LineItemDTO
     */
    @Mapping(target = "lineId", source = "invoiceLineId")
    @Mapping(target = "type", expression = "java(detail.getType().name())")
    @Mapping(target = "sparePart", source = "sparePart")
    InvoiceSummaryDTO.LineItemDTO toLineItemDTO(InvoiceDetail detail);

    /**
     * Map SparePart sang SparePartBasicDTO
     */
    @Mapping(target = "sparepartId", source = "sparepartId")
    @Mapping(target = "partName", source = "partName")
    @Mapping(target = "description", source = "description")
    InvoiceSummaryDTO.SparePartBasicDTO toSparePartBasicDTO(SparePart sparePart);
}
