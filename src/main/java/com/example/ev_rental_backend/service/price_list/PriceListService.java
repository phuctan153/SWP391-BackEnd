package com.example.ev_rental_backend.service.price_list;

import com.example.ev_rental_backend.dto.price_list.PriceListResponseDto;

import java.util.List;

public interface PriceListService {
    List<PriceListResponseDto> getAllSpareParts();
}
