package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class UserRequestDTOs
{
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\d{3}-\\d{4}-\\d{4}$\n");

    public record UpdateDTO(
            String nickname,
            String email,
            String phoneNumber,
            String birthday,
            String gender,
            Long imageId
    )
    {
        public boolean validate() throws BaseException
        {
            if(nickname == null || email == null || imageId == null) throw new BaseException(UserErrorCode.MISSING_REQUIRED_PARAMETER);
            if(nickname.length() < 2 || 20 < nickname.length()) throw new BaseException(UserErrorCode.NICKNAME_NOT_VALID);
            if(!EMAIL_PATTERN.matcher(email).matches()) throw new BaseException(UserErrorCode.EMAIL_NOT_VALID);
            if(!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) throw new BaseException(UserErrorCode.PHONE_NUMBER_NOT_VALID);
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

}
