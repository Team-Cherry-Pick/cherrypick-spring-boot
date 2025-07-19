package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;

import java.time.LocalDate;
import java.util.regex.Pattern;


public record UserUpdateRequestDTO(
        String nickname,
        String birthday,
        String gender,
        Long imageId
)
{

    public boolean validate() throws BaseException
    {
        if(nickname.length() < 2 || 20 < nickname.length()) throw new BaseException(UserErrorCode.NICKNAME_NOT_VALID);
        try{
            LocalDate.parse(birthday); // 유효하지 않은 날짜면 오류가 뜸.
        } catch (Exception e) {
            throw new BaseException(UserErrorCode.BIRTHDAY_NOT_VALID);
        }
        try{
            Gender.valueOf(gender); // 파싱 실패 시 오류
        }catch (Exception e) {
            throw new BaseException(UserErrorCode.GENDER_NOT_VALID);
        }

        return true;
    }

}