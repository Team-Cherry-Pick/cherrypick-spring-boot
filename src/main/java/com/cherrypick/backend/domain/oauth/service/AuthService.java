package com.cherrypick.backend.domain.oauth.service;

import com.cherrypick.backend.domain.oauth.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.oauth.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service @RequiredArgsConstructor @Slf4j
public class AuthService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

    private final String REFRESH_TOKEN_KEY_NAME = "RT:user:";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        var oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao 등

        // oauthId를 찾아냄.
        String oauthId = oauth2User.getAttribute("id").toString();
        var user = userRepository.findUserByOauthId(oauthId);

        User loginUser = null;
        if(user.isPresent()){
            log.info("흠1");
            // 저장된 유저 불러오기
            loginUser = user.get();

            log.info(":::: 기존 유저가 로그인 하였습니다 : " + user.toString());
            return OAuth2UserDTO.from(loginUser, false);
        }
        else{
            // 신규 유저 만들기, 추후 로그인 방법이 늘어나면 ENUM으로 처리하는 것도 고려.
            // oauth2.0 응답에서 데이터를 추출한다.
            if(provider.equals("kakao")) loginUser = User.fromKakao(oauth2User);
            else throw new BaseException(UserErrorCode.UNDEFINED_OAUTH_PROVIDER);

            // 닉네임 달아주기
            loginUser.setNickname(getRandomNickname(loginUser.getNickname()));
            // 신규 유저 저장
            userRepository.save(loginUser);
            log.info(":::: 신규 유저입니다. 로그인되었습니다. : " + user.toString());
            return OAuth2UserDTO.from(loginUser, true);
        }

    }

    @Override
    public void setAttributesConverter(Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        super.setAttributesConverter(attributesConverter);

    }

    // 닉네임을 좀 더 까리하게 만들어줍니다.
    public String getRandomNickname(String originalNickname)
    {
        var adjectiveList = List.of("멋있는 ", "대단한 ", "알뜰한 ", "네모난 ", "귀여운 ", "깜찍한 ", "듬직한 ", "깔롱한 ", "늠름한 ", "살뜰한 ", "짜릿한 ", "행복한 ", "소중한 ", "유능한 ", "강력한 ", "유연한 ", "쌈뽕한 ");
        int randomAdj = (int) Math.round(Math.random() * adjectiveList.size());

        // 닉네임 만들기, 존재하는 닉네임이라면 다시 만들어줌.
        String newNickName = null;
        do{
            newNickName = adjectiveList.get(randomAdj) + originalNickname;
        }
        while(userRepository.findUserByNickname(newNickName).isPresent());

        return newNickName;
    }

    public void saveResfreshToken(Long userId, String refreshToken)
    {
        // 토큰의 지속시간은 1주일
        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY_NAME+userId.toString() , refreshToken, Duration.ofMinutes(7 * 24 * 60));
    }

    public String loadRefreshToken(Long userId)
    {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_NAME+userId.toString()))
                .map(Object::toString)
                .orElseThrow(() -> new BaseException(UserErrorCode.REFRESH_TOKEN_EXPIRED)) ;
    }

    public AuthResponseDTOs.AccessToken refreshAccessToken(Long userId, String refreshToken)
    {
        // 서버에 갖고 있는 토큰과 쿠키의 토큰이 다르다면
        if(!refreshToken.equals(loadRefreshToken(userId)))
            throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);

        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return AuthResponseDTOs.AccessToken.builder()
                .accessToken(jwtUtil.createAccessToken(user.getUserId(), user.getRole(), user.getNickname()))
                .forYou("null값")
                .build();
    }

}
