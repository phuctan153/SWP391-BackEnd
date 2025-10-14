package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import com.example.ev_rental_backend.dto.payment.MomoIPNResponse;
import com.example.ev_rental_backend.service.payment.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/momo")
@Slf4j
public class MomoController {

    private final MomoService momoService;

    @PostMapping("/create")
    public CreateMomoResponse createMomo() {
        return momoService.createMomo();
    }

    @PostMapping("/ipn")
    public MomoIPNResponse handleMomoIPN(@RequestBody MomoIPNRequest request) {
        log.info("🔔 Received Momo IPN: {}", request);

        try {
            momoService.handleMomoIPN(request);
            return MomoIPNResponse.builder()
                    .resultCode(0)
                    .message("Confirm Success")
                    .build();
        } catch (Exception e) {
            log.error("❌ Error processing Momo IPN: {}", e.getMessage());
            return MomoIPNResponse.builder()
                    .resultCode(1)
                    .message("Confirm Failed: " + e.getMessage())
                    .build();
        }
    }
}
