package com.cherrypick.backend.domain.user.dto;

import java.util.regex.Pattern;

public class UserRequestDTOs
{
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public record UpdateDTO(
            String nickname,
            String email,
            String imageURL
    )
    {
        public boolean validate()
        {
            if(nickname == null || email == null || imageURL == null) return false;
            if(nickname.length() < 2 || 20 < nickname.length()) return false;
            if(!EMAIL_PATTERN.matcher(email).matches()) return false;

            return true;
        }

    }

}
