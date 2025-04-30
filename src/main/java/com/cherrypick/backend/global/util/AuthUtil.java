package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil
{
    private AuthUtil(){

    }

    public static boolean isAuthenticated()
    {
        return  SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    public static AuthenticationDetailDTO getUserDetail(){
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try{
            return (AuthenticationDetailDTO) principal;
        } catch (Exception e) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }
    }


}
