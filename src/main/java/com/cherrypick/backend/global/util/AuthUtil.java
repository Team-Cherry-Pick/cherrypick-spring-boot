package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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



}
