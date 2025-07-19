package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class UserRequestDTOs
{
    public record DeleteRequestDTO(String reason){}


}
