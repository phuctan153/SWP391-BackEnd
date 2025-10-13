package com.example.ev_rental_backend.service;

import com.example.ev_rental_backend.entity.Renter;
import com.example.ev_rental_backend.repository.RenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final RenterRepository renterRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Lấy thông tin từ Google
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // Nếu chưa tồn tại thì tạo mới user trong DB
        Renter renter = renterRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    Renter newRenter = Renter.builder()
                            .googleId(googleId)
                            .email(email)
                            .fullName(name)
                            .authProvider(Renter.AuthProvider.GOOGLE)
                            .status(Renter.Status.PENDING_VERIFICATION) // mặc định pending
                            .isBlacklisted(false)
                            .build();
                    return renterRepository.save(newRenter);
                });

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "sub" // dùng claim "sub" làm id
        );
    }
}
