package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.user.entity.Role;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Builder
public record AuthenticationDetailDTO(
        Long userId,
        String nickname,
        Role role
)
{
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(role);
    }
}
