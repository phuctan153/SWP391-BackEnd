package com.example.ev_rental_backend.service.price_list;

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
    public List<PriceList> getAllSpareParts() {
        List<PriceList> spareParts = priceListRepository.findByPriceType(PriceList.PriceType.SPARE_PART);
        log.info("📦 Fetched {} spare parts from price_list", spareParts.size());
        return spareParts;
    }
}
