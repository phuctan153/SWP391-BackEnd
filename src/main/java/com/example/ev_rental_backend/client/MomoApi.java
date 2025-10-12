package com.example.ev_rental_backend.client;

import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "momo", url = "${momo.end-point}")
public interface MomoApi {

    @PostMapping("/create")
    CreateMomoResponse createMomo(@RequestBody CreateMomoRequest createMomoRequest);
}
