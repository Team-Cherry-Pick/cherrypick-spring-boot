package com.cherrypick.backend.domain.auth.application;

import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.auth.presentation.dto.RegisterDTO;
import com.cherrypick.backend.domain.auth.presentation.dto.UserEnvDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.CacheKeyUtil;
import com.cherrypick.backend.global.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service @RequiredArgsConstructor @Slf4j
public class Oauth2ClientService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // [0] OAuth 서버로부터 유저 데이터를 불러와 변수 초기화
        var oauth2User = super.loadUser(userRequest);
        String oauthId = oauth2User.getAttribute("id").toString();
        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao 등

        // [1] oauthId로 유저를 조회.
        var user = userRepository.findUserByOauthId(oauthId);

        // [2] 존재하는 유저일 시 즉시 변환해 반환 / 신규 유저일 시 데이터를 받아 반환
        return user.map(u -> OAuth2UserDTO.from(u, false)).orElseGet(() -> {
            var userAttr = oauth2User.getAttributes();
            return OAuth2UserDTO.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .isNewUser(true)
                    .email(Optional.ofNullable((HashMap<String, String>)userAttr.get("kakao_account")).map(p -> p.get("email")).get().toString())
                    .build();
        });

    }

    @Override
    public void setAttributesConverter(Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        super.setAttributesConverter(attributesConverter);

    }



}
