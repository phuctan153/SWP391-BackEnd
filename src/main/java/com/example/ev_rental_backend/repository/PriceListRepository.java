package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    Optional<PriceList> findByPriceType(PriceList.PriceType priceType);
}
