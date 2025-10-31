package com.cherrypick.backend.domain.user.service;

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
import com.cherrypick.backend.global.log.domain.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service @RequiredArgsConstructor
public class UserRegisterUsecase {

    private final RegisterTokenProvider registerTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    private final BadgeService badgeService;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final RefreshCookieFactory refreshCookieFactory;
    private final UserLogService logService;

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
        var user = RegistUserFactory.extractUser(registerToken.oauthId(), registerToken.provider(), dto);

        // [3] 신규 유저를 저장
        var savedUser = userRepository.save(user);

        // [4] 유저의 사진을 등록
        if(dto.imageId() >= 0) imageService.attachImage(savedUser.getUserId(), List.of(dto.imageId()), ImageType.USER);

        // [5] 엑세스 토큰과 리프레시 토큰을 생성
        String accessToken = accessTokenProvider.createToken(savedUser.getUserId(), savedUser.getRole(), savedUser.getNickname());
        String refreshToken = refreshTokenProvider.createToken(savedUser.getUserId(), registerToken.userEnv().deviceId());

        // [6] Refresh 토큰과 유저 환경을 저장
        refreshTokenStore.initializeToken(savedUser.getUserId(), deviceId, refreshToken, registerToken.userEnv());

        // [7] 액세스 토큰과 쿠키에 담긴 리프레시 토큰 문자열을 반환
        String refreshCookie = refreshCookieFactory.createRefreshCookie(refreshToken);

        // [8] 기본 뱃지 등록
        badgeService.registerBadge(user.getUserId(), 1L);
        badgeService.equipBadge(user.getUserId(), 1L);

        // [9] 회원가입 로그
        logService.userRegisterLog(
                savedUser.getUserId(),
                savedUser.getNickname(),
                savedUser.getEmail(),
                savedUser.getOauthId(),
                "SUCCESS"
        );

        return new AuthResponseDTOs.TokenResponse(accessToken, refreshCookie);
    }



}
