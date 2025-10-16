package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.payment.CreateMomoRequest;
import com.example.ev_rental_backend.dto.payment.CreateMomoResponse;
import com.example.ev_rental_backend.dto.payment.MomoIPNRequest;
import com.example.ev_rental_backend.dto.payment.MomoIPNResponse;
import com.example.ev_rental_backend.service.payment.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/momo")
@Slf4j
public class MomoController {

    private final MomoService momoService;

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleMomoIPN(
            @RequestBody MomoIPNRequest request) {

        log.info("📨 Received Momo IPN callback: orderId={}", request.getOrderId());

        Map<String, Object> response = new HashMap<>();

        try {
            momoService.handleMomoIPN(request);

            // Momo yêu cầu response 204 No Content
            response.put("status", 0);
            response.put("message", "Success");

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("❌ Error processing Momo IPN: {}", e.getMessage(), e);

            response.put("status", -1);
            response.put("message", "Failed: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
