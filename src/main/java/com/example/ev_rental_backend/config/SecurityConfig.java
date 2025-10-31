package com.example.ev_rental_backend.config;

import com.example.ev_rental_backend.config.jwt.JwtAuthFilter;
import com.example.ev_rental_backend.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Bật CORS và tắt CSRF
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Tắt form login và HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ Phân quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/stations/**",
                                "/api/momo/**",
                                "/api/vehicle/**",
                                "/api/vehicles/**",
                                "/api/vehicle-models/**",
                                "/api/bookings/**",
                                "/api/payments/**",
                                "/api/invoices/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/v3/api-docs/swagger-config",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/api/renter/**").hasRole("RENTER")
                        .anyRequest().authenticated()
                )

                // ✅ Cấu hình OAuth2 login (Google)
                .oauth2Login(oauth -> oauth
                        .loginPage("/api/auth/google")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("https://swp-391-frontend-mu.vercel.app/homepage", true)
                )

                // ✅ JWT filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ Stateless session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ Logout redirect
                .logout(logout -> logout
                        .logoutSuccessUrl("https://swp-391-frontend-mu.vercel.app/login")
                        .permitAll()
                );

        return http.build();
    }

    // ✅ Cấu hình CORS CHUẨN
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ⚡ Cho phép các domain frontend
        configuration.setAllowedOrigins(List.of(
                "https://swp-391-frontend-mu.vercel.app",
                "https://nonpending-lelia-ballistically.ngrok-free.dev",
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        // ⚡ Cho phép các method cơ bản
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ⚡ Cho phép các header cần thiết
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // ⚡ Expose thêm header (Swagger và file download)
        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));

        // ⚡ Cho phép gửi cookie, JWT
        configuration.setAllowCredentials(true);

        // ⚡ Cache CORS 1h
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
