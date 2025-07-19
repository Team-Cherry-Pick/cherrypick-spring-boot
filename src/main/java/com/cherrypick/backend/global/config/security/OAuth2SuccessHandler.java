package com.cherrypick.backend.global.config.security;

import com.cherrypick.backend.domain.auth.application.AuthService;
import com.cherrypick.backend.domain.auth.domain.vo.UserEnv;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.auth.presentation.dto.UserEnvDTO;
import com.cherrypick.backend.domain.auth.application.Oauth2ClientService;
import com.cherrypick.backend.global.util.JwtUtil;
import com.cherrypick.backend.global.util.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.util.Base64;

@Component @Slf4j @RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LogService logService;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // [0] 등록되어 있는 유저 정보 로드
        OAuth2UserDTO userInfo = (OAuth2UserDTO) authentication.getPrincipal();

        // [1] 유저 환경 로드 및 변수 초기화
        String userEnvStr = new String(Base64.getUrlDecoder().decode(request.getParameter("state")));
        var userEnvArr = userEnvStr.split("\\|");
        var origin = userEnvArr[0];
        var redirect = userEnvArr[1];
        UserEnv userEnv = new UserEnv(userEnvArr[2], userEnvArr[3], userEnvArr[4], userEnvArr[5]);


        // [2] 새로운 유저 여부에 따라서 분기
        String token = null;
        if(userInfo.isNewUser()) {
            // [3-1] 신규 유저라면 등록 토큰을 반환
            token = authService.createRegisterToken(userInfo, userEnv);
        }
        else{
            // [3-2] 기존 유저라면 리프레시 토큰 발급
            var tokens = authService.tokensForLoginUser(userInfo, userEnv);

            // [3-2-1] 리프레시 토큰은 쿠키로, 액세스 토큰은 추후 파라미터에
            response.addHeader("Set-Cookie", tokens.refreshTokenCookie());
            token =  tokens.accessToken();
        }

        // [4] 프론트엔드 URL을 빌드
        String frontendUrl = UriComponentsBuilder
                .fromUriString(origin + "/login-success")
                .queryParam("token", token)
                .queryParam("isNewUser", userInfo.isNewUser())
                .queryParam("redirect", redirect)
                .queryParam("email", userInfo.email())
                .build()
                .toUriString();

        // [5] 유저 환경을 로그로 남김
        logService.loginLog(
                userInfo.isNewUser(),
                userInfo.provider(),
                userInfo.userId(),
                userEnv.deviceId(),
                userEnv.os(),
                userEnv.browser(),
                userEnv.version()
        );

        // [6] 프론트엔드에 응답
        response.sendRedirect(frontendUrl); // 클라이언트를 해당 주소로 리디렉트

    }

}
