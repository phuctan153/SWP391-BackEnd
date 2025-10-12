package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.service.payment.MomoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/momo")
public class MomoController {

    private final MomoService momoService;

    @PostMapping("/create")
    public CreateMomoResponse createMomo() {
        return momoService.createMomo();
    }

    @GetMapping("/ipn-handler")
    public String ipnHandler(@RequestParam Map<String, String> request) {
        Integer resultCode = Integer.valueOf(request.get("resultCode"));
        return resultCode == 0 ? "Payment Successfully" : "Payment Failed";
    }
}
