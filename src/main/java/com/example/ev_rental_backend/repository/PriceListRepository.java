package com.example.ev_rental_backend.repository;

import com.example.ev_rental_backend.entity.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    @Query("SELECT p FROM PriceList p WHERE p.priceType = :type AND p.stockQuantity > 0")
    List<PriceList> findAvailableByType(@Param("type") PriceList.PriceType type);

    Optional<PriceList> findByPriceType(PriceList.PriceType priceType);
}
