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

        var oauth2User = super.loadUser(userRequest);
        System.out.println(oauth2User.getAttributes().toString());
        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao 등

        // oauthId를 찾아냄.
        String oauthId = oauth2User.getAttribute("id").toString();
        var user = userRepository.findUserByOauthId(oauthId);

        User loginUser = null;
        if(user.isPresent()){
            // 저장된 유저 불러오기
            loginUser = user.get();

            return OAuth2UserDTO.from(loginUser, false);
        }
        else{
            // DB에 저장하지 않는다. 때문에 OAuth2UserDTO 에 oauthId와 provider만 담는다.
            var userAttr = oauth2User.getAttributes();
            return OAuth2UserDTO.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .isNewUser(true)
                    .email(Optional.ofNullable((HashMap<String, String>)userAttr.get("kakao_account")).map(p -> p.get("email")).get().toString())
                    .build();
        }
    }

    @Override
    public void setAttributesConverter(Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        super.setAttributesConverter(attributesConverter);

    }



}
