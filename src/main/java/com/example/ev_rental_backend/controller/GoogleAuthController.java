package com.example.ev_rental_backend.controller;

import com.example.ev_rental_backend.dto.ApiResponse;
import com.example.ev_rental_backend.dto.login.LoginResponseDTO;
import com.example.ev_rental_backend.service.renter.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/google")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> googleLogin(@RequestBody Map<String, Object> googleData) {
        try {
            String sub = (String) googleData.get("sub");
            String email = (String) googleData.get("email");
            String name = (String) googleData.get("name");
            String picture = (String) googleData.get("picture");
            System.out.println("GOOGLE DATA RECEIVED: " + googleData);

            ApiResponse<LoginResponseDTO> response = googleAuthService.handleGoogleLogin(sub, email, name, picture);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    ApiResponse.<LoginResponseDTO>builder()
                            .status("error")
                            .code(400)
                            .data(null)
                            .build()
            );
        }

    }
}
