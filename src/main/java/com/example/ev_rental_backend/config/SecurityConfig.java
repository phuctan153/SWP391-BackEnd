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
                // ✅ Cho phép CORS và tắt CSRF
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Không kích hoạt login mặc định (form hoặc oauth)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // ✅ Quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",      // đăng ký / login
                                "/api/stations/**",  // public cho bản đồ
                                "/api/momo/**",
                                "/api/vehicle/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/v3/api-docs/swagger-config",
                                "/error",
                                "/api/vehicles/**",// tạm thời public để test xe (sẽ đổi lại sau)
                                "/api/bookings/**", // tạm thời public để test xe (sẽ đổi lại sau)
                                "/api/vehicle-models/**", // tạm thời public để test xe (sẽ đổi lại sau)
                                "/api/payments/**", // tạm thời public để test xe (sẽ đổi lại sau)
                                "/api/invoices/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers("/api/renter/**").hasRole("RENTER")
                        .anyRequest().authenticated()
                )

                // ✅ Chỉ bật OAuth2 khi user chủ động login Google
                .oauth2Login(oauth -> oauth
                        .loginPage("/api/auth/google") // custom login URL
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("https://swp-391-frontend-mu.vercel.app/homepage", true)
                )

                // ✅ JWT Filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ✅ Stateless session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ Logout
                .logout(logout -> logout
                        .logoutSuccessUrl("https://swp-391-frontend-mu.vercel.app/login")
                        .permitAll()
                );

        return http.build();
    }

    // ✅ CORS cấu hình
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://nonpending-lelia-ballistically.ngrok-free.dev",
                "https://swp-391-frontend-mu.vercel.app",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
