package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import jakarta.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
public class AuthUtil
{
    private AuthUtil(){

    }

    public static boolean isAuthenticated()
    {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof AuthenticationDetailDTO;
    }

    public static AuthenticationDetailDTO getUserDetail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }

        var principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO)) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }

        return (AuthenticationDetailDTO) principal;
    }

    // 유저의 권한을 전환.
    // TODO : 다중 권한 구조로 바꿔야함.
    public static void changeAuthority(Role role) {

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        // 기존의 저장된 유저 정보
        AuthenticationDetailDTO principal = (AuthenticationDetailDTO)currentAuth.getPrincipal();


        List<GrantedAuthority> updatedAuthorities = List.of(
                new SimpleGrantedAuthority(role.name()) // 바꿀 권한
        );

        // 새로운 유저 정보
        var newPrincipal = AuthenticationDetailDTO.builder()
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
