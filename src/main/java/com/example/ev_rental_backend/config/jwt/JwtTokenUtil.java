package com.example.ev_rental_backend.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {
    private static final String SECRET_KEY = "mysecretkeyforjwtshouldbeatleast32characters";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 5; // 5 giờ

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ✅ Sinh token chứa cả userId + email + role
    public String generateTokenWithRoleAndId(Long userId, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // email làm subject
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Lấy toàn bộ claim
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Lấy email (subject)
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ✅ Lấy role
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ Lấy userId
    public Long extractUserId(String token) {
        Object id = extractAllClaims(token).get("userId");
        return (id instanceof Integer) ? ((Integer) id).longValue() : (Long) id;
    }

    // ✅ Kiểm tra token hợp lệ
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
