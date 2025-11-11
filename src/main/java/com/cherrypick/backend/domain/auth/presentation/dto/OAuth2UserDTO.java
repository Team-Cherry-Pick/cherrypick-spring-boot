package com.cherrypick.backend.domain.auth.presentation.dto;

import com.cherrypick.backend.domain.user.entity.User;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OAuth2 인증 사용자 DTO (다중 권한 지원)
 */
@Builder
public record OAuth2UserDTO(
        Long userId,
        String oauthId,
        String provider,
        String nickname,
        String email,
        Set<String> roles,  // 역할 목록 (예: ["ADMIN", "CLIENT"])
        boolean isNewUser
) implements OAuth2User
{

    // 엔티티로부터 DTO를 만든다.
    public static OAuth2UserDTO from(User user, boolean isNewUser) {
        // User.getRoleNames()는 ["ADMIN", "CLIENT"] 형태 반환
        Set<String> roles = user.getRoleNames();

        return OAuth2UserDTO.builder()
                .userId(user.getUserId())
                .oauthId(user.getOauthId())
                .provider(user.getProvider())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .roles(roles)
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) return Set.of();

        // Spring Security에 전달 시 ROLE_ prefix 추가
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return oauthId;
    }
}
