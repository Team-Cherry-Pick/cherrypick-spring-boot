package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.user.dto.UserDetailDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil
{
    private AuthUtil(){

    }

    public static UserDetailDTO getUserDetail(){
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try{
            return (UserDetailDTO) principal;
        } catch (Exception e) {
            throw new BaseException(UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED);
        }
    }


}
