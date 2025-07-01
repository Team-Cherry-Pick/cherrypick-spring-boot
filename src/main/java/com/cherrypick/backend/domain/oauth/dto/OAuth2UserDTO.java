package com.cherrypick.backend.domain.oauth.dto;

import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.entity.User;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder
public record OAuth2UserDTO(
        Long userId,
        String oauthId,
        String provider,
        String nickname,
        Role role,
        boolean isNewUser
) implements OAuth2User
{

    // TODO : 다중 권한 구조로 전환 필요

    // 엔티티로부터 DTO를 만든다.
    public static OAuth2UserDTO from(User user, boolean isNewUser) {
        return OAuth2UserDTO.builder()
                .userId(user.getUserId())
                .oauthId(user.getOauthId())
                .provider(user.getProvider())
                .nickname(user.getNickname())
                .role(user.getRole())
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        else return List.of(role);
    }

    @Override
    public String getName() {
        return oauthId;
    }
}
