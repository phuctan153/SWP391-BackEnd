package com.example.ev_rental_backend.service.price_list;

import com.example.ev_rental_backend.dto.price_list.PriceListResponseDto;
import com.example.ev_rental_backend.entity.PriceList;
import com.example.ev_rental_backend.repository.PriceListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceListServiceImpl implements PriceListService {

    private final PriceListRepository priceListRepository;

    @Override
    public List<PriceListResponseDto> getAllSpareParts() {
        return priceListRepository.findByPriceType(PriceList.PriceType.SPARE_PART)
                .stream()
                .map(p -> PriceListResponseDto.builder()
                        .priceId(p.getPriceId())
                        .itemName(p.getItemName())
                        .description(p.getDescription())
                        .unitPrice(p.getUnitPrice())
                        .stockQuantity(p.getStockQuantity())
                        .sparePartType(p.getSparePartType() != null ? p.getSparePartType().name() : "OTHER")
                        .build())
                .toList();
    }

}
