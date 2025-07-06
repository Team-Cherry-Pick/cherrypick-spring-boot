package com.cherrypick.backend.domain.oauth.service;

import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.oauth.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.oauth.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.oauth.dto.RegisterDTO;
import com.cherrypick.backend.domain.oauth.dto.UserEnvDTO;
import com.cherrypick.backend.domain.user.dto.UserUpdateRequestDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
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
public class AuthService extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;
    private final ImageService imageService;

    private final String REFRESH_TOKEN_KEY_NAME = "user:token:refresh";

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

    // 유저를 등록 완료시키는 로직
    @Transactional
    public AuthResponseDTOs.AccessToken userRegisterComplete(RegisterDTO dto, HttpServletResponse response){

        var registerToken = jwtUtil.getRegisterTokenPayload(dto.registerToken());
        if(userRepository.findUserByOauthId(registerToken.oauthId()).isPresent())
        {
            throw new BaseException(UserErrorCode.ALREADY_REGISTERED_USER);
        }

        var updateDTO = dto.updateDTO();

        // 신규 유저 만들기, 추후 로그인 방법이 늘어나면 ENUM으로 처리하는 것도 고려.
        // oauth2.0 응답에서 데이터를 추출한다.
        User user = null;
        if(registerToken.provider().equals("kakao")) {
            user = User.builder()
                    .oauthId(registerToken.oauthId())
                    .provider(registerToken.provider())
                    .nickname(updateDTO.nickname())
                    .email(updateDTO.email())
                    .gender(Gender.valueOf(updateDTO.gender()))
                    .birthday(LocalDate.parse(updateDTO.birthday()))
                    .status(UserStatus.ACTIVE)
                    .role(Role.CLIENT)
                    .build();
        }
        else throw new BaseException(UserErrorCode.UNDEFINED_OAUTH_PROVIDER);

        // 신규 유저 저장
        var savedUser = userRepository.save(user);

        // 기존 이미지 삭제, 새 이미지 등록
        imageService.deleteImageByUserId(savedUser.getUserId());
        imageService.attachImage(savedUser.getUserId(), List.of(updateDTO.imageId()), ImageType.USER);

        // 사진 업데이트
        var profileImage = imageService.getImageByUserId(savedUser.getUserId());

        // 엑세스 토큰을 전달.
        String accessToken = jwtUtil.createAccessToken(savedUser.getUserId(), savedUser.getRole(), savedUser.getNickname());

        // 리프레시 토큰 심어주기
        var refreshToken = jwtUtil.createRefreshToken(savedUser.getUserId());
        initializeResfreshToken(savedUser.getUserId(), registerToken.userEnv(), refreshToken);
        response.addHeader("Set-Cookie", jwtUtil.createRefreshCookie(refreshToken).toString());

        return new AuthResponseDTOs.AccessToken(accessToken);
    }

    // 닉네임을 좀 더 까리하게 만들어줍니다.
    public String getRandomNickname(String originalNickname)
    {
        var adjectiveList = List.of(
                "멋있는 ", "대단한 ", "알뜰한 ", "네모난 ", "귀여운 ", "깜찍한 ",
                "듬직한 ", "깔롱한 ", "늠름한 ", "살뜰한 ", "짜릿한 ", "행복한 ", "소중한 ",
                "유능한 ", "강력한 ", "유연한 ", "쌈뽕한 ", "재미난 ", "성실한 ", "날렵한 ");

        int randomAdj = (int) Math.round(Math.random() * adjectiveList.size());

        // 닉네임 만들기, 존재하는 닉네임이라면 다시 만들어줌.
        String newNickName = null;
        do{
            newNickName = adjectiveList.get(randomAdj) + originalNickname;
        }
        while(userRepository.findUserByNickname(newNickName).isPresent());

        return newNickName;
    }

    public void saveResfreshToken(Long userId, String deviceId, String refreshToken)
    {
        String key = REFRESH_TOKEN_KEY_NAME + ":" +userId.toString() + ":" + deviceId;

        redisTemplate.opsForHash().put( key, "token", refreshToken);
        redisTemplate.expire(key, Duration.ofMinutes(jwtUtil.refreshValidPeriod));
    }

    // 최초
    public void initializeResfreshToken(Long userId, UserEnvDTO userEnvDTO, String refreshToken)
    {
        // 토큰의 지속시간은 1주일
        String deviceId = userEnvDTO.deviceId();
        String key = REFRESH_TOKEN_KEY_NAME + ":" +userId.toString() + ":" + deviceId;

        redisTemplate.opsForHash().put( key, "token", refreshToken);
        redisTemplate.opsForHash().put( key, "userEnv", userEnvDTO.toJson());
        redisTemplate.expire(key, Duration.ofMinutes(jwtUtil.refreshValidPeriod));
    }

    public String loadRefreshToken(Long userId, String deviceId)
    {
        String key = REFRESH_TOKEN_KEY_NAME + ":" +userId.toString() + ":" + deviceId;

        return Optional.ofNullable(redisTemplate.opsForHash().get(key, "token"))
                .map(Object::toString)
                .orElseThrow(() -> new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    public UserEnvDTO loadUserEnv(Long userId, String deviceId){
        String key = REFRESH_TOKEN_KEY_NAME + ":" +userId.toString() + ":" + deviceId;
        var userEnv = (String) redisTemplate.opsForHash().get( key, "userEnv");
        return UserEnvDTO.fromJson(userEnv);
    }

    public AuthResponseDTOs.AccessToken refreshAccessToken(Long userId, String deviceId, String refreshToken)
    {
        // 서버에 갖고 있는 토큰과 쿠키의 토큰이 다르다면
        if(!refreshToken.equals(loadRefreshToken(userId,deviceId)))
            throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);

        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        return AuthResponseDTOs.AccessToken.builder()
                .accessToken(jwtUtil.createAccessToken(user.getUserId(), user.getRole(), user.getNickname()))
                .build();
    }

}
