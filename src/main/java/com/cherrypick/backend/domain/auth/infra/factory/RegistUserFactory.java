package com.cherrypick.backend.domain.auth.infra.factory;


import com.cherrypick.backend.domain.auth.domain.vo.token.RegisterTokenPayload;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.RegisterDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;

import java.time.LocalDate;

public class RegistUserFactory {

    private RegistUserFactory() {}

    public static User extractUser(String oauthId, String provider, RegisterDTO dto){

        User user = null;
        if(provider.equals("kakao")) {
            user = User.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .nickname(dto.nickname())
                    .email(dto.email())
                    .gender(Gender.valueOf(dto.gender()))
                    .birthday(LocalDate.parse(dto.birthday()))
                    .status(UserStatus.ACTIVE)
                    .role(Role.CLIENT)
                    .build();
        }
        else
        {
            System.out.println(provider);
            throw new BaseException(UserErrorCode.UNDEFINED_OAUTH_PROVIDER);
        }


        return user;
    }
}
