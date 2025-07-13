package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

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

    // 유저의 권한을 전환.
    // TODO : 다중 권한 구조로 바꿔야함.
    public static void changeAuthority(Role role) {

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        // 기존의 저장된 유저 정보
        AuthenticatedUser principal = (AuthenticatedUser)currentAuth.getPrincipal();


        List<GrantedAuthority> updatedAuthorities = List.of(
                new SimpleGrantedAuthority(role.name()) // 바꿀 권한
        );

        // 새로운 유저 정보
        var newPrincipal = AuthenticatedUser.builder()
                .userId(principal.userId())
                .nickname(principal.nickname())
                .role(role)
                .build();

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal,                        // 변경된 Role을 가진 DTO
                currentAuth.getCredentials(),
                updatedAuthorities                   // 갱신된 권한 목록
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }


}
