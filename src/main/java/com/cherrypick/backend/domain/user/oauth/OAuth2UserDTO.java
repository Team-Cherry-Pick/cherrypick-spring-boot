package com.cherrypick.backend.domain.user.oauth;

import com.cherrypick.backend.domain.user.entity.Role;
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
        String nickname,
        Role role
) implements OAuth2User
{

    // 엔티티로부터 DTO를 만든다.
    public static OAuth2UserDTO from(User user) {
        return OAuth2UserDTO.builder()
                .userId(user.getUserId())
                .oauthId(user.getOauthId())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public String getName() {
        return oauthId;
    }
}
