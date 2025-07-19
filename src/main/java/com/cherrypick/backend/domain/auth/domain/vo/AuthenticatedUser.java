package com.cherrypick.backend.domain.auth.domain.vo;

import com.cherrypick.backend.domain.user.enums.Role;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Builder
public record AuthenticatedUser(
        Long userId,
        String nickname,
        Role role
)
{
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(role);
    }
}
