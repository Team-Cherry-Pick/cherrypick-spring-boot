package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;

import java.util.regex.Pattern;

public class UserRequestDTOs
{
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public record UpdateDTO(
            String nickname,
            String email,
            Long imageId
    )
    {
        public boolean validate() throws BaseException
        {
            if(nickname == null || email == null || imageId == null) throw new BaseException(UserErrorCode.MISSING_REQUIRED_PARAMETER);
            if(nickname.length() < 2 || 20 < nickname.length()) throw new BaseException(UserErrorCode.NICKNAME_NOT_VALID);
            if(!EMAIL_PATTERN.matcher(email).matches()) throw new BaseException(UserErrorCode.EMAIL_NOT_VALID);

            return true;
        }

    }

}
