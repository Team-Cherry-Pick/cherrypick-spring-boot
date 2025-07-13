package com.cherrypick.backend.domain.auth.application;

import com.cherrypick.backend.domain.auth.domain.vo.token.RefreshTokenPayload;
import com.cherrypick.backend.domain.auth.infra.factory.RefreshCookieFactory;
import com.cherrypick.backend.domain.auth.infra.factory.RegistUserFactory;
import com.cherrypick.backend.domain.auth.infra.jwt.AccessTokenProvider;
import com.cherrypick.backend.domain.auth.infra.jwt.RefreshTokenProvider;
import com.cherrypick.backend.domain.auth.infra.jwt.RegisterTokenProvider;
import com.cherrypick.backend.domain.auth.infra.store.RefreshTokenStore;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.RegisterDTO;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RequiredArgsConstructor @Service
public class AuthService {

    private final RegisterTokenProvider registerTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final AccessTokenProvider accessTokenProvider;

    private final RefreshTokenStore refreshTokenStore;

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final RefreshCookieFactory refreshCookieFactory;


    // 유저를 등록 완료시키는 로직
    @Transactional
    public AuthResponseDTOs.TokenResponse userRegisterComplete(RegisterDTO dto){

        // [0] 레지스터 토큰 검증 및 변수 초기화
        var registerToken = registerTokenProvider.getTokenPayload(dto.registerToken());
        var deviceId = registerToken.userEnv().deviceId();

        // [1] 중복 유저 검사
        if(userRepository.findUserByOauthId(registerToken.oauthId()).isPresent())
        {
            throw new BaseException(UserErrorCode.ALREADY_REGISTERED_USER);
        }

        // [2] 저장할 유저 객체 생성, 현재 kakao만 지원.
        var user = RegistUserFactory.extractUser(registerToken.oauthId(), registerToken.oauthId(), dto);

        // [3] 신규 유저를 저장
        var savedUser = userRepository.save(user);

        // [4] 유저의 사진을 등록
        imageService.attachImage(savedUser.getUserId(), List.of(dto.imageId()), ImageType.USER);

        // [5] 엑세스 토큰과 리프레시 토큰을 생성
        String accessToken = accessTokenProvider.createToken(savedUser.getUserId(), savedUser.getRole(), savedUser.getNickname());
        String refreshToken = refreshTokenProvider.createToken(savedUser.getUserId(), registerToken.userEnv().deviceId());

        // [6] Refresh 토큰과 유저 환경을 저장
        refreshTokenStore.initializeToken(savedUser.getUserId(), deviceId, refreshToken, registerToken.userEnv());

        // [7] 액세스 토큰과 쿠키에 담긴 리프레시 토큰 문자열을 반환
        String refreshCookie = refreshCookieFactory.createRefreshCookie(refreshToken);
        return new AuthResponseDTOs.TokenResponse(accessToken, refreshCookie);
    }


    // 액세스 토큰 재발급 로직
    public AuthResponseDTOs.TokenResponse refreshAccessToken(String clientDeviceId, String clientRefreshToken)
    {

        // [0] 리프레시 토큰을 파싱, 변수 초기화
        RefreshTokenPayload payload = refreshTokenProvider.getPayload(clientRefreshToken);
        var userId = payload.userId();
        var deviceId = payload.deviceId();

        // [1] 리프레시 토큰의 deviceId와 수신 받은 deviceId가 같은지 검증. (다르다면 오류)
        if(!deviceId.equals(clientDeviceId)) throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);

        // [2] userId / deviceId 를 기반으로 서버에 저장된 리프레시 토큰을 로드.
        String storedToken = refreshTokenStore.loadToken(userId, deviceId);

        // [3] 요청으로 받은 토큰과 저장되어 있던 토큰이 같은지 검증.
        if(!clientRefreshToken.equals(storedToken)) throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);

        // [4] 토큰의 ID를 바탕으로 DB에서 유저 객체 로드.
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // [5] 엑세스 토큰과 리프레시 토큰을 발급.
        var accessToken = accessTokenProvider.createToken(userId, user.getRole(), user.getNickname());
        var refreshToken = refreshTokenProvider.createToken(userId, deviceId);

        // [6] 리프레시 토큰은 저장.
        refreshTokenStore.saveToken(userId, deviceId, refreshToken);

        // [7] 액세스 토큰과 쿠키에 담긴 리프레시 토큰 문자열을 반환
        String refreshCookie = refreshCookieFactory.createRefreshCookie(refreshToken);
        return new AuthResponseDTOs.TokenResponse(accessToken, refreshCookie);
    }

    // 로그아웃용 바로 만료되는 리프레시 토큰 쿠키
    public String createLogoutToken()
    {
        // [0] 바로 만료되는 리프레시 쿠키 문자열을 반환
        return refreshCookieFactory.createExpiringRefreshCookie();
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


}
