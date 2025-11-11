package com.cherrypick.backend.domain.auth.domain.vo;

import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 인증된 사용자 정보 VO
 * 다중 권한 지원
 */
@Builder
public record AuthenticatedUser(
        Long userId,
        String nickname,
        Set<String> roles  // 역할 목록 (예: ["ADMIN", "CLIENT"])
)
{
    /**
     * Spring Security용 GrantedAuthority 변환 (ROLE_ prefix 자동 추가)
     */
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    /**
     * 특정 역할 보유 여부 확인
     * @param roleName 역할 이름 (예: "ADMIN")
     */
    public boolean hasRole(String roleName) {
        return roles.contains(roleName);
    }
}
