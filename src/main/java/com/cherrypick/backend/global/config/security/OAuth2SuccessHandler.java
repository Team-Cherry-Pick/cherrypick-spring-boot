package com.cherrypick.backend.global.config.security;

import com.cherrypick.backend.domain.oauth.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.oauth.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.oauth.dto.UserEnvDTO;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.global.util.JWTUtil;
import com.cherrypick.backend.global.util.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;
import java.util.Base64;

@Component @Slf4j @RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AuthService authService;
    private final LogService logService;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2UserDTO userInfo = (OAuth2UserDTO) authentication.getPrincipal();

        String userEnvStr = new String(Base64.getUrlDecoder().decode(request.getParameter("state")));
        var userEnv = userEnvStr.split("\\|");
        var redirect = userEnv[0];
        UserEnvDTO userEnvDTO = new UserEnvDTO(userEnv[1], userEnv[2], userEnv[3], userEnv[4]);

        String token = null;
        if(userInfo.isNewUser()) {
            // 등록 토큰을 반환
            token = newUserToken(userInfo, userEnvDTO);
        }
        else{
            // 리프레시 토큰
            var refreshToken = registeredUserRefreshToken(userInfo, userEnvDTO);
            response.addHeader("Set-Cookie", refreshToken.toString());

            // 엑세스 토큰
            token =  jwtUtil.createAccessToken(userInfo.userId(), userInfo.role(), userInfo.nickname());
        }

        String origin = request.getHeader("Origin");
        String frontendUrl = UriComponentsBuilder
                .fromUriString(origin + "/login-success")
                .queryParam("token", token)
                .queryParam("isNewUser", userInfo.isNewUser())
                .queryParam("redirect", redirect)
                .queryParam("email", userInfo.email())
                .build()
                .toUriString();

        logService.loginLog(
                userInfo.isNewUser(),
                userInfo.provider(),
                userInfo.userId(),
                userEnvDTO.deviceId(),
                userEnvDTO.os(),
                userEnvDTO.browser(),
                userEnvDTO.version()
        );

        response.sendRedirect(frontendUrl); // 클라이언트를 해당 주소로 리디렉트

    }

    private String newUserToken(OAuth2UserDTO oAuth2UserDTO, UserEnvDTO userEnvDTO) {

        var token = AuthResponseDTOs.RegisterTokenDTO.builder()
                        .oauthId(oAuth2UserDTO.oauthId())
                        .provider(oAuth2UserDTO.provider())
                        .userEnv(userEnvDTO)
                        .build();

        return jwtUtil.createRegisterToken(token);
    }

    // 리프레시 토큰 쿠키를 만들어서 반환.
    private ResponseCookie registeredUserRefreshToken(OAuth2UserDTO userInfo, UserEnvDTO userEnvDTO) {
        var refreshToken = jwtUtil.createRefreshToken(userInfo.userId());
        authService.initializeResfreshToken(userInfo.userId(), userEnvDTO, refreshToken);

        return jwtUtil.createRefreshCookie(refreshToken);
    }


}
