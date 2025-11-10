package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

@Slf4j
public class AuthUtil
{
    private AuthUtil(){

    }

    public static boolean isAuthenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return false;

        var principal = authentication.getPrincipal();
        return principal instanceof AuthenticatedUser;
    }


    public static AuthenticatedUser getUserDetail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }

        var principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser)) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }

        return (AuthenticatedUser) principal;
    }

    /**
     * 유저의 권한을 변경 (다중 권한 지원)
     * @param roles 새로운 역할 집합 (예: Set.of("ADMIN", "CLIENT"))
     */
    public static void changeAuthority(Set<String> roles) {
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser principal = (AuthenticatedUser) currentAuth.getPrincipal();

        // 새로운 유저 정보
        var newPrincipal = AuthenticatedUser.builder()
                .userId(principal.userId())
                .nickname(principal.nickname())
                .roles(roles)
                .build();

        // AuthenticatedUser.getAuthorities()가 자동으로 GrantedAuthority 생성
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal,
                currentAuth.getCredentials(),
                newPrincipal.getAuthorities()  // AuthenticatedUser가 제공하는 authorities 사용
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }


}
